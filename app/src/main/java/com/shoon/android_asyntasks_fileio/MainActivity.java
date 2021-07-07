package com.shoon.android_asyntasks_fileio;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    EditText et;
    TextView tv;
    String strShift="0",
            strToProcess="",
            strProcessed="";
    Button bt;

    ProgressDialog progressBar;
    private int progressBarStatus = 0;
    private Handler progressBarHandler = new Handler();
    private int iProgress = 0;

    private ArrayList<String> strarFileList(Context context){
        AssetManager assetManager = getResources().getAssets();
        String[] fileList1 = null;
        try {
            fileList1 = assetManager.list("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] fileList2=context.getCacheDir().list();
        ArrayList<String> fileList = new ArrayList<String>();

        fileList.addAll( Arrays.asList( fileList1 ) );
        fileList.addAll( Arrays.asList( fileList2 ) );
        return fileList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        tv=findViewById( R.id.text_main );
      //  strToProcess=getResources().getString( R.string.contents_shifted);
        bt=findViewById( R.id.button_for_result );
        final Context context=getApplicationContext();

        ArrayList<String> fileList=strarFileList( context);

        ArrayAdapter<String> aa=new ArrayAdapter<String>( context,android.R.layout.simple_spinner_dropdown_item,fileList );
        aa.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        final Spinner sp=findViewById( R.id. spinner);
        sp.setAdapter( aa );
        sp.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
              @Override
              public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                  String strFile=(String)parent.getItemAtPosition( position );
                  InputStream is=null;
                  try {
                      is=context.getAssets().open( strFile );
                  } catch (IOException e) {
                      e.printStackTrace();
                      try {
                          String str=context.getCacheDir().getPath()+"/"+strFile;

                          is= new FileInputStream(new File(str));
                      } catch (FileNotFoundException e1) {
                          e1.printStackTrace();
                      }
                  }
                  System.out.printf( String.valueOf( position ) );

                  InputStreamReader isr=new InputStreamReader( is);
                  BufferedReader bfr = new BufferedReader( isr );
                  StringBuilder sb=new StringBuilder(  );

                 String strTemp="";
                  while(true){
                      try {
                          if (!((strTemp = bfr.readLine()) != null)) break;
                          sb.append( strTemp);
                      } catch (IOException e) {
                          e.printStackTrace();
                      }
                    //  System.out.println(strToProcess);
                  }
                  try {
                      bfr.close();
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
                  strToProcess=sb.toString();
                  tv.setText( sb );
              }

              @Override
              public void onNothingSelected(AdapterView<?> parent) {

              }
          });

        findViewById( R.id.buttonSave ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strFileName= String.valueOf( sp.getSelectedItem() );
                File file= new File(strFileName+"shift"+strShift);
                File fileTmp = null;
                try {


                    fileTmp=File.createTempFile(  strFileName+"shift"+strShift+"_", ".txt", context.getCacheDir() );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    FileWriter fileWriter=new FileWriter(fileTmp);
                    BufferedWriter bw=new BufferedWriter( fileWriter );
                    bw.write( strProcessed );
                    try {
                        fileWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {

                }



            }
        } );

        et=findViewById( R.id.input_number );
        bt.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar = new ProgressDialog(v.getContext());
                progressBar.setCancelable(true);
                progressBar.setMessage("Processing ...");
                progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressBar.setProgress(0);
                progressBar.setMax(100);
                progressBar.show();
                //reset progress bar and filesize status
                progressBarStatus = 0;
                iProgress = 0;
                new Thread(new Runnable() {
                    Handler handler = new Handler();

                    public void run() {
                        while (progressBarStatus < 100) {
                            // performing operation
                            progressBarStatus = doOperation();
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            // Updating the progress bar

                            progressBarHandler.post(  new Runnable() {
                                public void run() {
                                    progressBar.setProgress(progressBarStatus);
                                }
                            });
                        }

                        if (progressBarStatus >= 100) {
                            // sleeping for 1 second after operation completed
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            // close the progress bar dialog
                            progressBar.dismiss();
                            handler.post( new Runnable() {
                                @Override
                                public void run() {
                                    tv.setText( strProcessed );
                                }
                            } );

                        }
                    }
                }).start();


            }
        } );




    }




    public int doOperation() {
        //The range of ProgressDialog starts from 0 to 10000

        String debug="";
        StringBuilder sTemp = new StringBuilder();
        if (strToProcess.equals( "" ))return 100;
        strShift=et.getText().toString();
        int shift=Integer.parseInt(strShift ); //obtain number to shift
        int len = strToProcess.length();
        // shift=8;
        char cTemp;
        int iPitch=len/4; //frequency to return value
        if(iProgress==0){
            strProcessed="";

        }
        for(int x=iProgress; x<len; x++){

            cTemp=strToProcess.charAt(x);
            if(cTemp>=65&&cTemp<=90){
                cTemp=(char)(cTemp+shift);
                debug+=(char)(cTemp-shift); //debug
                if (cTemp > 'Z') {
                    sTemp.append(  (char) (cTemp-26));
                }else{
                    sTemp.append( (char)cTemp);
                }
            }else if(cTemp>=97&&cTemp<=122){
                cTemp=(char)(cTemp+shift);
                debug+=(char)(cTemp-shift); //debug
                if (cTemp > 'z') {
                    sTemp.append(  (char) (cTemp-26));
                }else {
                    sTemp.append(  (char) cTemp);
                }
            }else{

                sTemp.append( (char)cTemp);
            }
            iProgress++;

            if(iProgress%iPitch==0) {
                strProcessed+=sTemp;

                return 100*iProgress/len;
            }

        }
        strProcessed+=sTemp;

        return 100;
    }//end of doOperation

}


