package com.example.jacob.android_threads;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.os.AsyncTask;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    ProgressBar progressBar;
    TextView textView;
    EditText editText;
    Context context;
    ImageButton buttonUpdate;
    offloadTask task;
    Spinner spinner;
    final ArrayList<String> itemArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Stetho.initializeWithDefaults(this);
        context = this;
        progressBar = findViewById(R.id.progress_bar);
        buttonUpdate = findViewById(R.id.button_update);
        spinner = findViewById(R.id.spinner);
        textView = findViewById(R.id.text_main_content);
        editText = findViewById(R.id.edit_shift);
        updateSpinnerList();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, itemArray);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setSelection(0, false);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BufferedReader bufferedReader = null;
                StringBuilder builder = new StringBuilder();
                InputStream stream = null;
                FileInputStream fStream = null;

                String selectedItem = (String) parent.getItemAtPosition(position);

                if (selectedItem.contains("_Shifted_")) {
                    editText.setText("0");
                    try {
                        fStream = new FileInputStream(new File(context.getCacheDir(), selectedItem));
                        int i = 0;
                        while ((i = fStream.read()) != -1) {
                            builder.append((char) i);
                        }
                        textView.setText(builder.toString());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (fStream != null) {
                            try {
                                fStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    try {
                        selectedItem += ".txt";
                        stream = context.getAssets().open(selectedItem);
                        InputStreamReader inputStreamReader = new InputStreamReader(stream);
                        bufferedReader = new BufferedReader(inputStreamReader);

                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            line += "\n";
                            builder.append(line);
                        }
                        String textToSend = builder.toString();
                        String shiftAmount = editText.getText().toString();
                        new offloadTask().execute(textToSend, shiftAmount);


                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String unShiftedString = getResources().getString(R.string.contents_shifted);
                String shiftAmount = editText.getText().toString();
                task = new offloadTask();
                task.execute(unShiftedString, shiftAmount);
            }
        });

        findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = spinner.getSelectedItem().toString() + "_Shifted_" + editText.getText() + ".txt";
                FileWriter writer = null;
                try {
                    File outputFile = File.createTempFile(fileName, null, context.getCacheDir());
                    writer = new FileWriter(outputFile);
                    writer.write(textView.getText().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                updateSpinnerList();
            }
        });

        findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (task != null) {
                    task.cancel(false);
                }
            }
        });


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (task != null) {
            task.cancel(false);
        }
    }

    public class offloadTask extends AsyncTask<String, Integer, String> {

        public static final int progressResolution = 50;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setMax(getResources().getString(R.string.contents_shifted).length());

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            textView.setText(s);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            progressBar.setVisibility(View.GONE);
            textView.setText(s);
            Toast.makeText(getApplicationContext(), "Processing Cancelled", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... params) {
            if (params[0] != null) {
                StringBuilder outputStringBuilder = new StringBuilder();
                int shift = Integer.parseInt(params[1]);
                if (shift < 0) {
                    shift = shift % -26;
                    shift = 26 + shift;
                }
                int loops = params[0].length();

                for (int i = 0; i < loops; ++i) {
                    char unicode = params[0].charAt(i);
                    if ((unicode >= 'A' && unicode <= 'Z') || ((unicode >= 'a' && unicode <= 'z'))) {
                        for (int n = 0; n < shift; ++n) {
                            ++unicode;
                            if (unicode > 'Z' && unicode < 'a') {
                                unicode = 'A';
                            }
                            if (unicode > 'z') {
                                unicode = 'a';
                            }
                        }
                    }

                    //Add some pi digits calculations to slow it down a little
//                    Log.i("piCalc=", pi_digits(100));

                    if (i % Math.round(loops / progressResolution) == 0) {
                        publishProgress(i);
                        if (isCancelled()) {
                            outputStringBuilder.append("...[Processing canceled.  Press Update to refresh.]");
                            return outputStringBuilder.toString();
                        }
                    }
                    outputStringBuilder.append(unicode);
                }
                return outputStringBuilder.toString();
            } else {
                return "";
            }

        }
    }

    void updateSpinnerList() {
        try {
            itemArray.clear();
            itemArray.add("");
            String[] items = getAssets().list("");
            for (String item : items) {
                if (item.contains("txt")) {
                    itemArray.add(item.replace(".txt", ""));
                }
            }
            //add items to spinner
            items = context.getCacheDir().list();
            for (String item : items) {
                if (item.contains("txt")) {
                    itemArray.add(item);
//                    itemArray.add(item.replace(".txt", ""));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static String pi_digits(int digits) {
        int SCALE = 10000;
        int ARRINIT = 2000;
        StringBuffer pi = new StringBuffer();
        int[] arr = new int[digits + 1];
        int carry = 0;

        for (int i = 0; i <= digits; ++i)
            arr[i] = ARRINIT;

        for (int i = digits; i > 0; i -= 14) {
            int sum = 0;
            for (int j = i; j > 0; --j) {
                sum = sum * j + SCALE * arr[j];
                arr[j] = sum % (j * 2 - 1);
                sum /= j * 2 - 1;
            }
            pi.append(String.format("%04d", carry + sum / SCALE));
            carry = sum % SCALE;
        }
        return pi.toString();
    }
}
