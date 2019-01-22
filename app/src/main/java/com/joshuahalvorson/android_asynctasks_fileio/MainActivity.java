package com.joshuahalvorson.android_asynctasks_fileio;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Trace;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView cipherTextView;
    private ProgressBar progressBar;
    private EditText shiftTextValue;
    private AsyncTask cipherTextTask;
    private Spinner spinner;
    Context context;


        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progress_bar);
        cipherTextView = findViewById(R.id.cipher_text);
        shiftTextValue = findViewById(R.id.shift_text_value);
        context = this;
        spinner = findViewById(R.id.file_select);
        createAdapter();

        findViewById(R.id.decode_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cipherText = cipherTextView.getText().toString();
                String shiftValue = shiftTextValue.getText().toString();
                cipherTextTask = (new CipherTextTask()).execute(cipherText, shiftValue);
            }
        });

        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = shiftTextValue.getText().toString() + spinner.getSelectedItem().toString();
                FileWriter fileWriter =  null;
                try {
                    File file = File.createTempFile(fileName, null, context.getCacheDir());
                    fileWriter = new FileWriter(file);
                    fileWriter.write(cipherTextView.getText().toString());
                    createAdapter();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if(fileWriter != null){
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

    private void createAdapter(){
        List<String> spinnerArray =  new ArrayList<>();
        try {
            String[] files = getAssets().list("");
            for(String s : files){
                if(s.substring(s.length() - 4).equals(".txt")){
                    spinnerArray.add(s);
                }
            }
            String[] cacheFiles = context.getCacheDir().list();
            for(String s : cacheFiles){
                if(s.contains(".txt")){
                    spinnerArray.add(s);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BufferedReader bufferedReader;
                StringBuilder sb = new StringBuilder();
                try{
                    String file = adapter.getItem(position);
                    if(file.contains(".tmp")){
                        File newFile = new File(context.getCacheDir(), file);
                        FileReader fileReader = new FileReader(newFile);
                        bufferedReader = new BufferedReader(fileReader);
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            sb.append(line);
                            sb.append("\n");
                        }
                        cipherTextView.setText(sb.toString());
                    }else{
                        InputStream inputStream = context.getAssets().open(file);
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        bufferedReader = new BufferedReader(inputStreamReader);
                        while(bufferedReader.readLine() != null){
                            sb.append(bufferedReader.readLine());
                        }
                        cipherTextView.setText(sb.toString());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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

    public class CipherTextTask extends AsyncTask<String, Integer, StringBuilder>{
        int lettersShifted = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressBar.setVisibility(View.VISIBLE);
            //progressBar.setMax(cipherTextView.getText().toString().length());
        }


        @Override
        protected StringBuilder doInBackground( String... strings) {
            Trace.beginSection("doInBackground");
            int shiftAmount = Integer.parseInt(shiftTextValue.getText().toString());
            String inputString = strings[0];
            if (inputString != null) {
                final StringBuilder newString = new StringBuilder();
                Reader stringReader = new StringReader(inputString);
                BufferedReader reader = new BufferedReader(stringReader);
                String line;
                try{
                    while ((line = reader.readLine()) != null) {

                        for(int i = 0; i < line.length(); i++){
                            char currentChar = strings[0].charAt(i);
                            if (shiftAmount >= 0) {
                                if (Character.isLowerCase(currentChar)) {
                                    newString.append((char) ('a' + (26 + currentChar - 'a' - shiftAmount % 26) % 26));
                                } else if (Character.isUpperCase(currentChar)) {
                                    newString.append((char) ('A' + (26 + currentChar - 'A' - shiftAmount % 26) % 26));
                                } else {
                                    newString.append(currentChar);
                                }
                            } else {
                                if (Character.isLowerCase(currentChar)) {
                                    newString.append((char) ('a' + ((currentChar - 'a' - shiftAmount) % 26)));
                                } else if (Character.isUpperCase(currentChar)) {
                                    newString.append((char) ('A' + ((currentChar - 'A' - shiftAmount) % 26)));
                                } else {
                                    newString.append(currentChar);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //for (int i = 0; i < strings[0].length(); i++) {

                    /*if(lettersShifted % 30 == 0){
                        publishProgress(lettersShifted);
                        if(isCancelled()){
                            progressBar.setVisibility(View.GONE);
                            cipherTextView.setText(newString);
                            return newString.toString();
                        }
                    }
                    lettersShifted++;*/
                //}
                return newString;
            }
            Trace.endSection();
            return null;
        }

        /*@Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }*/



        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(StringBuilder string) {
            //progressBar.setVisibility(View.GONE);
            cipherTextView.setText(string.toString());
        }
    }
}
