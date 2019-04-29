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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      /*    cipherTextView = findViewById(R.id.cipher_data_text);
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
        }); */
      context = this;
      spinner = findViewById(R.id.spinner);
      cipherTextView = findViewById(R.id.cipher_data_text);

      String[] assets = null;
      ArrayList<String> textAssets = new ArrayList<>();
        try {
            assets = getAssets().list("");
            for (int i = 0; i < assets.length; i++) {
                if (assets[i].endsWith(".txt")) {
                    textAssets.add(assets[i]);
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
                try {
                    String selectedItem = (String) parent.getItemAtPosition(position);
                    InputStream inputStream = context.getAssets().open(selectedItem);
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

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
                        || (c < 'a' && c > 'a' + shift )) {
                    c += 26;
                }
                builder.append(c);
                publishProgress(shifts);
                shifts++;
            }
            // System.out.println(newString);
            return builder.toString();
        }
    }
}