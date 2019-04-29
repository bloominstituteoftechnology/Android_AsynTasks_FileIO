package com.example.israel.android_threads;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private int MAX_SHIFT = 25;
    private DecryptorAsyncTask decryptorAsyncTask;
    private TextView textTextView;
    private EditText shiftEditText;
    private TextView statusTextView;
    private ProgressBar decryptionProgressBar;
    private Spinner textFilesSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textTextView = findViewById(R.id.text_view_text);
        shiftEditText = findViewById(R.id.edit_text_shift);
        statusTextView = findViewById(R.id.text_view_status);
        decryptionProgressBar = findViewById(R.id.progress_bar_decryption);
        textFilesSpinner = findViewById(R.id.spinner_text_files);

        populateSpinner();

        // clear cache button
        findViewById(R.id.button_clear_cache).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // @TODO might be better to do this on other thread
                deleteCache(MainActivity.this);
                populateSpinner();
            }
        });

        // start decryption button
        findViewById(R.id.button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (decryptorAsyncTask != null) { // still decrypting
                    return;
                }

                String shiftStr = shiftEditText.getText().toString();
                try {
                    int shift = Integer.parseInt(shiftStr);
                    if (shift < -MAX_SHIFT || shift > MAX_SHIFT) { // shift oob
                        statusTextView.setText(getResources().getString(R.string.text_error_shift_oob));
                        return;
                    }

                    decryptorAsyncTask = new DecryptorAsyncTask(shift);
                    String encryptedStr;
                    String selectedTextFile = (String)textFilesSpinner.getSelectedItem();
                    if (selectedTextFile.contains(".tmp")) { // a cached file
                        encryptedStr = readCachedFile(selectedTextFile);
                    } else { // an asset file
                        encryptedStr = readAssetFile(selectedTextFile);
                    }

                    if (encryptedStr == null) {
                        return;
                    }

                    decryptionProgressBar.setMax(encryptedStr.length());
                    decryptorAsyncTask.execute(encryptedStr);

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    statusTextView.setText(getResources().getString(R.string.text_error_shift_invalid));
                }
            }
        });

        // save text button
        findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try { // save file
                    String txtFile = ((String)textFilesSpinner.getSelectedItem()) + "_" + shiftEditText.getText().toString() + "_";
                    File file = File.createTempFile(txtFile, null, getCacheDir());
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(textTextView.getText().toString());
                    fileWriter.close();

                    // add to spinner
                    ArrayAdapter<String> arrayAdapter = (ArrayAdapter)textFilesSpinner.getAdapter();
                    arrayAdapter.add(file.getName());
                    arrayAdapter.notifyDataSetChanged();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void populateSpinner() {

        ArrayList<String> files = new ArrayList<>();

        // from assets
        try {
            // get all .txt file in assets folder
            String[] assets = getAssets().list(""); // all files/directory in main/assets
            if (assets != null) {
                ArrayList<String> txtAssets = new ArrayList<>();
                for (String asset : assets) {
                    int dotIndex = asset.lastIndexOf('.');
                    if (dotIndex == -1 || dotIndex == asset.length() - 1) {
                        continue;
                    }

                    String fileExt = asset.substring(dotIndex);
                    if (fileExt.equals(".txt")) {
                        txtAssets.add(asset);
                    }
                }

                files.addAll(txtAssets);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // from cache dir
        String[] cachedFiles = getCacheDir().list();
        for (String cachedFile : cachedFiles) {
            if (cachedFile.contains(".tmp")) {
                files.add(cachedFile);
            }
        }

        // list of string that will be shown in the spinner
        ArrayAdapter<String> txtAssetsArrayAdapter = new ArrayAdapter<String>(this, R.layout.txt_asset_spinner_item_layout, files);
        //txtAssetsArrayAdapter.setDropDownViewResource(R.layout.txt_asset_spinner_item_layout);

        textFilesSpinner.setAdapter(txtAssetsArrayAdapter);
        textFilesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTextFile = (String)textFilesSpinner.getSelectedItem();
                if (selectedTextFile.contains(".tmp")) { // a cached file
                    textTextView.setText(readCachedFile(selectedTextFile));
                } else { // an asset file
                    textTextView.setText(readAssetFile(selectedTextFile));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Nullable
    private String readAssetFile(String assetFile) {
        try {
            InputStream inputStream = getAssets().open(assetFile);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();

            return stringBuilder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    private String readCachedFile(String cachedFile) {
        try {
            File file = new File(getCacheDir(), cachedFile);
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();

            return stringBuilder.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onDestroy() {
        // cancel to avoid leak
        if (decryptorAsyncTask != null) {
            decryptorAsyncTask.cancel(false);
        }

        super.onDestroy();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    private class DecryptorAsyncTask extends AsyncTask<String, Integer, String> {

        public DecryptorAsyncTask(int shift) {
            this.shift = shift;
        }

        public static final int ALPHABET_SIZE = 26;
        public static final int MIN_LOWERCASE_CHAR = 'a';
        public static final int MAX_LOWERCASE_CHAR = 'z';
        public static final int MIN_UPPERCASE_CHAR = 'A';
        public static final int MAX_UPPERCASE_CHAR = 'Z';

        private int shift;

        @Override
        protected void onPreExecute() {
            statusTextView.setText(getResources().getString(R.string.text_status_decrypting));
            decryptionProgressBar.setVisibility(View.VISIBLE);

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            String encryptedString = strings[0];
            int onePercentSize = Math.max((int)(encryptedString.length() * 0.01), 1);

            StringBuilder decryptedStr =  new StringBuilder(encryptedString.length());
            for (int i = 0; i < encryptedString.length(); ++i) {
                char c = encryptedString.charAt(i);

                if (!Character.isLetter(c)) { // do not shift non alphabet
                    decryptedStr.append(c);
                    continue;
                }

                int shiftedCInt = c + shift;
                if (Character.isLowerCase(c)) { // originally a lowercase
                    if (shiftedCInt < MIN_LOWERCASE_CHAR) { // under-shifted
                        shiftedCInt += ALPHABET_SIZE;
                    } else if (shiftedCInt > MAX_LOWERCASE_CHAR) { // over-shifted
                        shiftedCInt -= ALPHABET_SIZE;
                    }
                } else {
                    if (shiftedCInt < MIN_UPPERCASE_CHAR) {
                        shiftedCInt += ALPHABET_SIZE;
                    } else if (shiftedCInt > MAX_UPPERCASE_CHAR) {
                        shiftedCInt -= ALPHABET_SIZE;
                    }
                }

                decryptedStr.append((char)shiftedCInt);

                int finishedSize = i + 1;
                if (finishedSize % onePercentSize == 0) {
                    publishProgress(finishedSize);
                    if (isCancelled()) {
                        break;
                    }
                }
            }

            return decryptedStr.toString();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            decryptionProgressBar.setProgress(values[0]);

            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (!isCancelled()) {
                decryptionProgressBar.setVisibility(View.INVISIBLE);
                statusTextView.setText(getResources().getString(R.string.text_status_idle));
                textTextView.setText(s);
                decryptorAsyncTask = null;
            }
        }

        @Override
        protected void onCancelled() {
            decryptionProgressBar.setVisibility(View.INVISIBLE);
            statusTextView.setText(getResources().getString(R.string.text_status_cancelled));
            textTextView.setText("");
            super.onCancelled();
        }
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) { e.printStackTrace();}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}
