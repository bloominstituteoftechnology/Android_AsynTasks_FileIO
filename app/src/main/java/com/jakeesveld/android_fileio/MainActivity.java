package com.jakeesveld.android_fileio;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ProgressBar progressBar;
    EditText editInput;
    Button buttonSubmit;
    TextView textCipher;
    DecryptThread decryptThread;
    Spinner spinner;
    Context context;
    Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progress_bar);
        editInput = findViewById(R.id.edit_input);
        buttonSubmit = findViewById(R.id.button_shift);
        textCipher = findViewById(R.id.text_view_cypher);
        spinner = findViewById(R.id.spinner);
        buttonSave = findViewById(R.id.button_save);
        context = this;

        try {
            String[] assetsStringArray = getAssets().list("");
            String[] cacheStringArray = context.getCacheDir().list();
            ArrayList<String> assetsArray = new ArrayList<>();
            if (assetsStringArray != null) {
                for(String item: assetsStringArray){
                    if(item.contains(".txt")){
                        assetsArray.add(item);
                    }
                }
            }
            if(cacheStringArray != null){
                for(String item: cacheStringArray){
                    if(item.contains(".txt")){
                        assetsArray.add(item);
                    }
                }
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_spinner_item, assetsArray);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(arrayAdapter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = editInput.getText().toString() + spinner.getSelectedItem().toString();
                try {
                    File newFile = File.createTempFile(fileName, null, context.getCacheDir());
                    FileWriter writer = new FileWriter(newFile);
                    writer.write(textCipher.getText().toString());
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BufferedReader reader = null;
                StringBuilder builder = new StringBuilder();
                try{
                    String item = (String) parent.getItemAtPosition(position);
                    InputStream stream = context.getAssets().open(item);
                    InputStreamReader inputStreamReader = new InputStreamReader(stream);
                    reader = new BufferedReader(inputStreamReader);
                    String line = reader.readLine();
                    while(line != null){
                        builder.append(line);
                        line = reader.readLine();
                    }
                    textCipher.setText(builder.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cipherString = textCipher.getText().toString();
                progressBar.setMax(cipherString.length());
                decryptThread = new DecryptThread();
                decryptThread.execute(cipherString);

                /*progressBar.setVisibility(View.VISIBLE);
                String cipherString = textCipher.getText().toString();
                int shiftTimes = Integer.parseInt(editInput.getText().toString());
                String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
                String UPPER_CASE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

                String shiftedCipher = "";
                for(int i = 0; i < cipherString.length(); i++){
                    if (ALPHABET.indexOf(cipherString.charAt(i)) != -1) {
                        int position = ALPHABET.indexOf(cipherString.charAt(i));
                        int shiftedPosition = (position + shiftTimes) % 26;
                        *//*if(shiftedPosition > 0){
                            shiftedPosition = ALPHABET.length() + shiftedPosition;
                        }*//*
                        char shiftedChar = ALPHABET.charAt(shiftedPosition);
                        shiftedCipher += shiftedChar;
                    }else if(UPPER_CASE_ALPHABET.indexOf(cipherString.charAt(i)) != -1){
                        int position = UPPER_CASE_ALPHABET.indexOf(cipherString.charAt(i));
                        int shiftedPosition = (position + shiftTimes) % 26;
                        char shiftedChar = UPPER_CASE_ALPHABET.charAt(shiftedPosition);
                        shiftedCipher += shiftedChar;
                    }else{
                        shiftedCipher += cipherString.charAt(i);
                    }

                }

                textCipher.setText(shiftedCipher);

                progressBar.setVisibility(View.GONE);*/


            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        decryptThread.cancel(true);
    }

    public class DecryptThread extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            buttonSubmit.setVisibility(View.GONE);
            editInput.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {

            int shiftTimes = Integer.parseInt(editInput.getText().toString());
            String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
            String UPPER_CASE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

            String shiftedCipher = "";
            StringBuilder builder = new StringBuilder(strings[0].length());
            for (int i = 0; i < strings[0].length(); i++) {
                char selectedChar = strings[0].charAt(i);
                if (ALPHABET.indexOf(selectedChar) != -1) {
                    int position = ALPHABET.indexOf(selectedChar);
                    int shiftedPosition = (position + shiftTimes) % 26;
                        /*if(shiftedPosition > 0){
                            shiftedPosition = ALPHABET.length() + shiftedPosition;
                        }*/
                    char shiftedChar = ALPHABET.charAt(shiftedPosition);
                    //shiftedCipher += shiftedChar;
                    builder.append(shiftedChar);
                    publishProgress(i);
                } else if (UPPER_CASE_ALPHABET.indexOf(selectedChar) != -1) {
                    int position = UPPER_CASE_ALPHABET.indexOf(selectedChar);
                    int shiftedPosition = (position + shiftTimes) % 26;
                    char shiftedChar = UPPER_CASE_ALPHABET.charAt(shiftedPosition);
                    //shiftedCipher += shiftedChar;
                    builder.append(shiftedChar);
                    publishProgress(i);
                } else {
                    //shiftedCipher += selectedChar;
                    builder.append(selectedChar);
                    publishProgress(i);
                }

            }
            shiftedCipher = builder.toString();
            return shiftedCipher;
        }

        @Override
        protected void onPostExecute(String s) {
            textCipher.setText(s);
            progressBar.setVisibility(View.GONE);
            buttonSubmit.setVisibility(View.VISIBLE);
            editInput.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onCancelled() {
            progressBar.setVisibility(View.GONE);
            buttonSubmit.setVisibility(View.VISIBLE);
            editInput.setVisibility(View.VISIBLE);
        }
    }
}
