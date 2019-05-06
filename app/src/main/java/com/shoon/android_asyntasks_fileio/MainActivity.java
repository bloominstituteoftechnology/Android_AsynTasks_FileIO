package com.shoon.android_asyntasks_fileio;


import android.app.ProgressDialog;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;




public class MainActivity extends AppCompatActivity {
    EditText et;
    TextView tv;
    String str;
    Button bt;
    ProgressDialog progressBar;
    private int progressBarStatus = 0;
    private Handler progressBarHandler = new Handler();
    private int iProgress = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        tv=findViewById( R.id.text_main );
        str=getResources().getString( R.string.contents_shifted);
        bt=findViewById( R.id.button_for_result );

        tv.setText(str);
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
                        // performing operation if file is downloaded,
                        if (progressBarStatus >= 100) {
                            // sleeping for 1 second after operation completed
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            // close the progress bar dialog
                            progressBar.dismiss();
                        }
                    }
                }).start();


            }
        } );




    }
    public int doOperation() {
        //The range of ProgressDialog starts from 0 to 10000

        String debug="";
        String sTemp =  et.getText().toString();
        if (sTemp.equals( "" ))return 100;

        int shift=Integer.parseInt( sTemp );
        sTemp="";
        int len = str.length();
        // shift=8;
        char cTemp;
        int iPitch=len/4; //frequency to return value
        for(int x = 0; x < len; x++){

            cTemp=str.charAt(x);
            if(cTemp>=65&&cTemp<=90){
                cTemp=(char)(cTemp+shift);
                debug+=(char)(cTemp-shift); //debug
                if (cTemp > 'Z') {
                    sTemp += (char) (cTemp-26);
                }else{
                    sTemp +=(char)cTemp;
                }
            }else if(cTemp>=97&&cTemp<=122){
                cTemp=(char)(cTemp+shift);
                debug+=(char)(cTemp-shift); //debug
                if (cTemp > 'z') {
                    sTemp += (char) (cTemp-26);
                }else {
                    sTemp += (char) cTemp;
                }
            }else{

                sTemp+=(char)cTemp;
            }
            iProgress++;

            if(iProgress%iPitch==0) {
                return 100*iProgress/len;
            }

        }
        tv.setText( sTemp );



        return 100;
    }//end of doOperation

}


