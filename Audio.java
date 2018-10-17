package com.hirunima.v_new_one;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Base64;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import 	java.lang.Short;
import 	java.lang.Number;
import java.util.Calendar;

/**
 * Created by hirunimaj on 7/21/2017.
 */

public class Audio extends Service{


    private static final int RECORDER_SAMPLERATE = 11025;

    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;

    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT ;

    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    private int bufferSize;
    private double lastLevel = 0;
    private long index = 0;
    private static final int SAMPLE_DELAY = 5;
    private int bufferReadResult = 1;
    private FileOutputStream os;
    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format
    short bufferreadings[] = new short[BufferElements2Rec];
    long finall=0;
    int count=0;
    long begin=0;
    private File output;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1; //Month start from 0
        int date = calendar.get(Calendar.DATE);
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        String filePath = "/sdcard/Mydata/" +"hit"+"_"+ month +"_" + date +"_" + hour +"_" + minute + ".csv";
        output = new File(filePath);
        if (output.exists()) output.delete();


        try {
            os = new FileOutputStream(output,true);
            //System.out.println("123456789");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        startRecord();

    }

    private void startRecord() {

        //recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
        //RECORDER_SAMPLERATE, RECORDER_CHANNELS,
        //RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, bufferSize);
        recorder.startRecording();

        isRecording = true;

        recordingThread = new Thread(new Runnable() {

            public void run() {


                while(recordingThread != null && !recordingThread.isInterrupted()){
                    //Let's make the thread sleep for a the approximate sampling time
                    //try{Thread.sleep(SAMPLE_DELAY);}catch(InterruptedException ie){ie.printStackTrace();}
                    readAudioBuffer();//After this call we can get the last value assigned to the lastLevel variable

                    long difference =finall-begin;

                    if (lastLevel>6000) {
                        //count +=1;
                        //if (count>=2) {
                        System.out.println("time"+difference);
                        System.out.println("Ball Hit  " + lastLevel+"time"+finall);
                        writehit(lastLevel);
                        stopRecord();
                        //}
                    }
                }
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    private void readAudioBuffer() {

        try {
            short[] buffer = new short[bufferSize];



            if (recorder != null) {

                // Sense the voice...

                bufferReadResult = recorder.read(buffer, 0, bufferSize);
                bufferreadings=buffer;
                double sumLevel = 0;
                begin = System.nanoTime();
                double max = Math.abs(buffer[0]);
                for (int i = 0; i < bufferReadResult; i++) {
                    sumLevel += Math.abs(buffer[i]);
                    if (max<Math.abs(buffer[i])){
                        max = Math.abs(buffer[i]);
                        index=i;
                    }
                }
                //lastLevel = Math.abs((sumLevel / bufferReadResult));
                lastLevel = Math.abs((max));
                finall=System.nanoTime();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writehit(double numb){


        try {
            Long tsLong = System.currentTimeMillis()/1000;
            String message=tsLong.toString()+","+Double.toString(numb)+","+"1"+"\n";

            os.write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecord() {
        // stops the recording activity
        if (null != recorder) {
            isRecording = false;


            recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }
    }


    @Override
    public void onDestroy() {

        if (null != recorder) {
            isRecording = false;


            recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }
        if(os != null) {
            try {
                os.flush();
                os.close();
            } catch (IOException e) {
                Log.i("Error", "Error closing file");
            }
        }
        super.onDestroy();
    }
}
