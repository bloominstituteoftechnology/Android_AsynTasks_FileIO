package com.example.fileio;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
	ProgressBar progressBar;
	EditText et;
	TextView tv;
	Spinner spinner;
	int progress;
	public static int key;
	String startingText;
	char[] upperCase = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	char[] lowerCase = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
	String[] txtFiles;
	BufferedReader bufferedReader = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv = findViewById(R.id.cypher_text);
		et = findViewById(R.id.shift_by_view);
		progressBar = findViewById(R.id.progressBar);
		spinner = findViewById(R.id.spinner_select_file);
		
		startingText =  tv.getText().toString();
		Log.i("test2", String.valueOf(startingText.length()));
		
		setSpinner();
		
		
		Button button = findViewById(R.id.button_Shift);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				progressBar.setVisibility(View.VISIBLE);
				key = Integer.parseInt(et.getText().toString());
				ThreadClass threadClass = new ThreadClass();
				threadClass.execute();
			}
		});
	}
	
	public void setSpinner(){
		String[] files = null;
		
		try {
			files = getAssets().list("");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int counter = 0;
		for(int i = 0; i < files.length; i++){
			if(files[i].contains(".txt")){
				counter++;
			}
		}
		
		txtFiles = new String[counter];
		
		int counter2 = 0;
		for(int j = 0; j < files.length; j++){
			if(files[j].contains(".txt")){
				txtFiles[counter2] = files[j];
				counter2++;
			}
		}
		
		final List<String> txtList = new ArrayList<>(Arrays.asList(txtFiles));
		final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item, txtList);
		spinnerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerArrayAdapter);
		
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				StringBuilder stringBuilder = new StringBuilder();
				
				String asset = (String)spinner.getItemAtPosition(position);
				InputStream inputStream = null;
				
				try {
					inputStream = getAssets().open(asset);
				} catch (IOException e) {
					e.printStackTrace();
				}
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				bufferedReader = new BufferedReader(inputStreamReader);
				
				try {
					String next = bufferedReader.readLine();
					while(next != null){
						stringBuilder.append(next);
						next = bufferedReader.readLine();
					}
					
					String output = stringBuilder.toString();
					
					tv.setText(output);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			
			}
		});
		
	}
	
	public class ThreadClass extends AsyncTask{
		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressBar.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected void onPostExecute(Object o) {
			super.onPostExecute(o);
			tv.setText(String.valueOf(o));
			progressBar.setVisibility(View.GONE);
		}
		
		@Override
		protected void onProgressUpdate(Object[] values) {
			super.onProgressUpdate(values);
			progressBar.setMax(startingText.length());
			progressBar.setProgress(progress);
		}
		
		@Override
		protected Object doInBackground(Object[] objects) {
			String text = startingText;
			char[] textCharArray = text.toCharArray();
			char[] textShifted = new char[textCharArray.length];
			String output = "";
			
			for(int i = 0; i < textCharArray.length; i++){
				for(int j = 0; j < lowerCase.length; j++){
					if(textCharArray[i] == upperCase[j]){
						textShifted[i] = upperCase[((j+key)%upperCase.length)];
						j=0;
						break;
					}else if(textCharArray[i] == lowerCase[j]) {
						textShifted[i] = lowerCase[((j + key) % lowerCase.length)];
						j=0;
						break;
					}
				}if(textShifted[i]=='\u0000'){textShifted[i] = textCharArray[i];}
				progress = i;
				publishProgress();
				output = output + String.valueOf(textShifted[i]);
				
				
			}return output;
			
		}
	}
	
	
}