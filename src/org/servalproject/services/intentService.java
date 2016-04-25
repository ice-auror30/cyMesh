package org.servalproject.services;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
/**
 * Created by Adit
 */

public class intentService extends Service {

    private static intentService mInstance = null;
    static String targetPackage;
    static String mAudioPath;
    static String mVideoPath;

    private intentService() {
        mInstance = this;
        instantiateFilePaths();

    }

    /**
     * Set the static file paths where we store collected audio and video
     */
    private void instantiateFilePaths()
    {

        String mBasePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mAudioPath = mBasePath +  "/TestAudioFile.3gp";
        mVideoPath = mBasePath + "/TestVideoFile.mkv";
    }

    /**
     * Singleton Format
     *
     * @return This object
     */
    public static synchronized intentService getInstance() {
        if (mInstance == null) {
            mInstance = new intentService();
        }
        return mInstance;
    }


    /**
     * Set the package of the app to target
     * @param targetPackage
     */
    public void setTargetPackage(String targetPackage)
    {

        this.targetPackage = targetPackage;

    }

    /**
     * This method handles executing the requests as they come in from the broadcast receiver
     * @param request
     */
    public synchronized void processRequest(String request)
    {
        //Todo based on team Discussion
        //Parse the request and then use one of the methods below to send the information to the appropriate package
        //Process request will call setTargetPackage and then the appropriate method below
    }
    /**
     * Sends a  information to the target package
     * @param information
     */
    private void sendString(String information) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, information);
        i.setType("text/plain");
        //The package to target in another application.
        i.setPackage(targetPackage);
        startActivity(i);
    }

    /**
     * Sends a path to the audio file location on the phone to the target package
     */
    private void sendAudio() {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, mAudioPath);
        i.setType("text/plain");
        //The package to target in another application.
        i.setPackage(targetPackage);
        startActivity(i);
    }

    /**
     * Sends a path to the video file location on the phone to the target package
     */
    private void sendVideo() {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, mVideoPath);
        i.setType("text/plain");
        //The package to target in another application.
        i.setPackage(targetPackage);
        startActivity(i);
    }

    /**
     * Sends sensor data as a string the target package
     */
    private void sendSensorData(String sensorString) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, sensorString);
        i.setType("text/plain");
        //The package to target in another application.
        i.setPackage(targetPackage);
        startActivity(i);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
