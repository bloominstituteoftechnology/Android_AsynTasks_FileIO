package com.example.patrickjmartin.android_asyntasks_fileio;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private Spinner spinner;
    private ArrayList<String> spinnerAry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        spinner = findViewById(R.id.file_select);
        addToSpinner();


        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, spinnerAry);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BufferedReader bufferedReader = null;
                StringBuilder sb = new StringBuilder();
                InputStream stream;

                try {
                    String file = adapter.getItem(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });




    }

    private void addToSpinner(){

        //Garbage collector once old ref removed
        spinnerAry = new ArrayList<String>();

        try {
            String[] assetFiles = getAssets().list("");
            for(String temp : assetFiles) {
                if(temp.contains(".txt")) {
                    spinnerAry.add(temp);
                }
            }

//            String[] cacheFiles = context.getCacheDir().list();
//            for(String temp : cacheFiles) {
//                if(temp.contains("txt")) {
//                    spinnerAry.add(temp);
//                }
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
