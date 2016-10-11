package org.servalproject.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Brad on 2/17/2016.
 */
public class RecordingService extends Service implements SurfaceHolder.Callback {
    MediaRecorder recorder;
    SurfaceHolder holder;
    boolean recording = false;
    boolean recorded = false;
    SurfaceView cameraView;
    public static final String RECORDING_FINISHED="org.servalproject.recordclick.FINISHED";
    public static final String START_CAMERA="START_CAMERA";
    private static final String TAG = "RecordingService";

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String SIDString = (String) intent.getExtras().get("localSIDString");
        Log.d(TAG, "Created. SIDString passed in: "+SIDString);
        cameraView = new SurfaceView(getApplicationContext());
        holder = cameraView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);
        WindowManager wm = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.UNKNOWN);
        wm.addView(cameraView, params);

        startStopRecording(SIDString);
        return START_STICKY;
    }

    private void initRecorder() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        recorder.setOrientationHint(90);

        CamcorderProfile profile = null;

        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_1080P)) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
            Log.d(TAG, "Profile set to 1080");
        }else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
            Log.d(TAG, "Profile set to 720");
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
        }else if(CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)){
            Log.d(TAG, "Profile set to 480");
            profile=CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        }else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_HIGH)) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            Log.d(TAG, "Profile set to HIGH");
        }
        if (profile != null){
            recorder.setProfile(profile);
        } else {
            Log.d(TAG, "Profile set to NULL");
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            recorder.setVideoEncodingBitRate(3000000);
        }

        Log.d(TAG, "initRecorder finished");
    }

    private void prepareRecorder() {
        SurfaceTexture surfaceTexture = new SurfaceTexture(10);
        Surface sv = new Surface(surfaceTexture);
        recorder.setPreviewDisplay(sv);
        try {
            recorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startStopRecording(String sid) {
        Log.d(TAG,"startStopRecording");
        TimerTask t = new TimerTask() {
            @Override
            public void run() {
                if (recording) {
                    Log.d(TAG, "Interrupted by new record request");
                    recorder.stop();
                    recorder.reset();
                    recorder.release();

                    Log.d(TAG,"Stopped");
                    recording = false;
                    recorded = true;

                    sendBroadcast(new Intent(RECORDING_FINISHED));
                }
            }
        };
        if(recorded){
            recorded = false;
        }
        if(!recording) {
            initRecorder();
            Log.d(TAG,"Output to: " + Environment.getExternalStorageDirectory() + File.separator
                    + Environment.DIRECTORY_DCIM + File.separator + sid + ".mp4");
            recorder.setOutputFile(Environment.getExternalStorageDirectory() + File.separator
                    + Environment.DIRECTORY_DCIM + File.separator + sid + ".mp4");
            recording = true;
            prepareRecorder();

            recorder.start();
            Timer myTimer = new Timer();

            myTimer.schedule(t, 5000);

        }
    }

    public void surfaceCreated(SurfaceHolder holder) {}

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {}

    public void surfaceDestroyed(SurfaceHolder holder) {}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onPause(){}

    @Override
    public void onDestroy() {
        recorder.stop();
        recording = false;

        super.onDestroy();
    }
}
