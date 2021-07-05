package com.vivekvishwanath.android_asyntasks_fileio;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.vivekvishwanath.android_asyntasks_fileio.R;

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
    Button shiftButton;
    TextView cipherTextView;
    EditText userInput;
    ProgressBar progressBar;
    int shift;
    Context context;
    Spinner spinner;
    Button saveButton;
    String shiftedString = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInput = findViewById(R.id.shift_input);
        shiftButton = findViewById(R.id.shift_button);
        progressBar = findViewById(R.id.progress_bar);
        shiftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cipher = cipherTextView.getText().toString();
                shift = Integer.parseInt(userInput.getText().toString());
                progressBar.setMax(cipher.length());
                new DecryptCypherAsync().execute(cipher);
            }
        });
        context = this;
        spinner = findViewById(R.id.spinner);
        cipherTextView = findViewById(R.id.cipher_data_text);
        saveButton = findViewById(R.id.save_button);

        String[] assets = null;
        ArrayList<String> textAssets = new ArrayList<>();
        try {
            assets = getAssets().list("");
            for (int i = 0; i < assets.length; i++) {
                if (assets[i].endsWith(".txt")) {
                    textAssets.add(assets[i]);
                }
            }
            String[] cacheFiles = context.getCacheDir().list();
            for (int i = 0; i < cacheFiles.length; i++) {
                if (cacheFiles[i].endsWith(".txt")) {
                    textAssets.add(cacheFiles[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item,
                textAssets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BufferedReader bufferedReader = null;
                StringBuilder stringBuilder = new StringBuilder();
                InputStream inputStream = null;
                FileInputStream fileInputStream = null;

                String selectedItem = (String) parent.getItemAtPosition(position);

                if (selectedItem.contains("_shifted_")) {
                    try {
                        fileInputStream = new FileInputStream(new File(context.getCacheDir(), selectedItem));
                        byte read;
                        do {
                            read = (byte) fileInputStream.read();
                            stringBuilder.append((char)read);
                        } while (read != -1);
                        String result = stringBuilder.toString();
                        cipherTextView.setText(result);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {

                    try {
                        inputStream = context.getAssets().open(selectedItem);
                        InputStreamReader isReader = new InputStreamReader(inputStream);
                        bufferedReader = new BufferedReader(isReader);
                        String nextLine;
                        do {
                            nextLine = bufferedReader.readLine();
                            stringBuilder.append(nextLine);
                        } while (nextLine != null);
                        String result = stringBuilder.toString();
                        cipherTextView.setText(result);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shiftedString != null) {
                    String fileName = spinner.getSelectedItem().toString() + "_shifted_" +
                            shift;
                    FileWriter fileWriter = null;
                    try {
                        File file = File.createTempFile(fileName, ".txt", context.getCacheDir());
                        fileWriter = new FileWriter(file);
                        fileWriter.write(shiftedString);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            fileWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }


    class DecryptCypherAsync extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {
            progressBar.setVisibility(View.GONE);
            cipherTextView.setText(s);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected String doInBackground(String... strings) {
            int shifts = 0;
            StringBuilder builder = new StringBuilder();
            char c;
            for (int i = 0; i < strings[0].length(); i++) {
                c = strings[0].charAt(i);
                if (Character.isLetter(c)) {
                    c = (char) (strings[0].charAt(i) + shift);
                }
                if ((c < 'A' && c > 'A' + shift)
                        || (c < 'a' && c > 'a' + shift)) {
                    c += 26;
                }
                builder.append(c);
                publishProgress(shifts);
                shifts++;
            }
            // System.out.println(newString);
            shiftedString = builder.toString();
            return builder.toString();
        }
    }
}