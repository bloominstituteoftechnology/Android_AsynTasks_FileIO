package com.joshuahalvorson.android_asynctasks_fileio;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView cipherTextView;
    private ProgressBar progressBar;
    private EditText shiftTextValue;
    private AsyncTask cipherTextTask;
    Context context;
    String[] items;


        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progress_bar);
        cipherTextView = findViewById(R.id.cipher_text);
        shiftTextValue = findViewById(R.id.shift_text_value);
        context = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.decode_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cipherText = cipherTextView.getText().toString();
                String shiftValue = shiftTextValue.getText().toString();
                cipherTextTask = (new CipherTextTask()).execute(cipherText, shiftValue);
            }
        });
        Spinner spinner = findViewById(R.id.file_select);
        List<String> spinnerArray =  new ArrayList<String>();
        try {
            String[] files = getAssets().list("");
            for(String s : files){
                if(s.substring(s.length() - 3).equals("txt")){
                    spinnerArray.add(s);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


    }

    @Override
    protected void onStop() {
        super.onStop();
        if(cipherTextTask != null){
            cipherTextTask.cancel(true);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public class CipherTextTask extends AsyncTask<String, Integer, String>{
        int lettersShifted = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setMax(cipherTextView.getText().toString().length());
        }


        @Override
        protected String doInBackground( String... strings) {
            int shiftAmount = Integer.parseInt(shiftTextValue.getText().toString());
            if (strings[0] != null) {
                StringBuilder newString = new StringBuilder();
                for (int i = 0; i < strings[0].length(); i++) {
                    if (shiftAmount >= 0) {
                        if (Character.isLowerCase(strings[0].charAt(i))) {
                            newString.append((char) ('a' + (26 + strings[0].charAt(i) - 'a' - shiftAmount % 26) % 26));
                        } else if (Character.isUpperCase(strings[0].charAt(i))) {
                            newString.append((char) ('A' + (26 + strings[0].charAt(i) - 'A' - shiftAmount % 26) % 26));
                        } else {
                            newString.append(strings[0].charAt(i));
                        }
                    } else {
                        if (Character.isLowerCase(strings[0].charAt(i))) {
                            newString.append((char) ('a' + ((strings[0].charAt(i) - 'a' - shiftAmount) % 26)));
                        } else if (Character.isUpperCase(strings[0].charAt(i))) {
                            newString.append((char) ('A' + ((strings[0].charAt(i) - 'A' - shiftAmount) % 26)));
                        } else {
                            newString.append(strings[0].charAt(i));
                        }
                    }
                    if(lettersShifted % 40 == 0){
                        publishProgress(lettersShifted);
                        if(isCancelled()){
                            progressBar.setVisibility(View.GONE);
                            cipherTextView.setText(newString);
                            return newString.toString();
                        }
                    }
                    lettersShifted++;
                }
                return newString.toString();
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }



        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(String string) {
            progressBar.setVisibility(View.GONE);
            cipherTextView.setText(string);
        }
    }
}
