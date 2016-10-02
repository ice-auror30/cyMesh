package org.servalproject.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.rhizome.FilteredCursor;
import org.servalproject.rhizome.MeshMS;
import org.servalproject.rhizome.Rhizome;
import org.servalproject.rhizome.RhizomeManifest_File;
import org.servalproject.servald.ServalD;
import org.servalproject.servaldna.BundleId;
import org.servalproject.servaldna.ServalDCommand;
import org.servalproject.servaldna.SubscriberId;

import java.io.File;

public class CameraService extends Service {

    public static String TAG = "CameraService";

    private String localSIDString;
    private String recorderSIDString;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        IntentFilter actionFilter = new IntentFilter();
        actionFilter.addAction(MeshMS.NEW_MESSAGES);
        actionFilter.addAction(MeshMS.START_RECORDING);
        actionFilter.addAction(RecordingService.RECORDING_FINISHED);
        this.registerReceiver(actionReceiver, actionFilter, Rhizome.RECEIVE_PERMISSION,
                null);

        IntentFilter dataFilter = new IntentFilter();
        dataFilter.addAction(Rhizome.ACTION_RECEIVE_FILE);
        try {
            dataFilter.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Log.e("RhizomeList", e.toString(), e);
        }
        this.registerReceiver(dataReceiver, dataFilter);

    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        if (intent.getExtras().getString("localSIDString") != null && !intent.getExtras().getString("localSIDString").equals("null")) {
            localSIDString = intent.getExtras().getString("localSIDString");
            Log.d(TAG, "onStartCommand. localSIDString passed in: " + localSIDString);
        }
        if (intent.getExtras().getString("recorderSIDString") != null && !intent.getExtras().getString("recorderSIDString").equals("null")) {
            recorderSIDString = intent.getExtras().getString("recorderSIDString");
            Log.d(TAG, "onStartCommand. recorderSIDString passed in: " + recorderSIDString);
        }
        return START_STICKY;
    }

    public void onDestroy(){
//        this.unregisterReceiver(dataReceiver);
//        this.unregisterReceiver(actionReceiver);
    }

    BroadcastReceiver actionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(MeshMS.NEW_MESSAGES)) {
                //TODO Recording is happening on every meshms received AND sent
                Log.d(TAG,"MeshMS has just registered a new message");
            }

            if(intent.getAction().equals(MeshMS.START_RECORDING)){
                if (localSIDString != null) {
                    Log.d(TAG, "actionReceiver Acting: record a video");
                    Log.d(TAG, "localSIDString: " + localSIDString);
                    Intent i = new Intent(getApplicationContext(), RecordingService.class);
                    i.putExtra("localSIDString", localSIDString);
                    startService(i);
//                } else {
//                    Log.d(TAG, "Broadcast Receiver Triggered on own message sent");
//                }
                }
            }
            if(intent.getAction().equals(RecordingService.RECORDING_FINISHED)) {
                Log.d(TAG,"Recording is finished, let's send the video");
                sendCapturedVideo();
            }
        }
    };

    BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Rhizome.ACTION_RECEIVE_FILE)) {
                Bundle b = intent.getExtras();
                if (recorderSIDString != null && intent.getType().contains("mp4") && b.getString("name").equals(recorderSIDString+".mp4")) {
                    Log.d(TAG, "datareceiver Acting: play the video");
                    Log.d(TAG, "recorderSIDString: " + recorderSIDString);
                    playVideo(b.getString("id"));
                }
            }
        }
    };

    private void playVideo(String bidString) {
        try {
            BundleId bid = new BundleId(bidString);
            File dir = Rhizome.getTempDirectoryCreated();
            final File temp = new File(dir, bid.toHex() + ".mp4");
            temp.delete();
            ServalDCommand.rhizomeExtractFile(bid, temp);
            String ext = temp.getName().substring(temp.getName().lastIndexOf(".") + 1);
            String contentType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(ext);
            Uri uri = Uri.fromFile(temp);
            Log.d(TAG, "Open uri='" + uri + "', contentType='" + contentType + "'");
            Intent newIntent = new Intent();
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.setAction(Intent.ACTION_VIEW);
            newIntent.setDataAndType(uri, contentType);
            getApplicationContext().startActivity(newIntent);
        } catch (Exception e) {
            Log.e(Rhizome.TAG, e.toString(), e);
            ServalBatPhoneApplication.context.displayToastMessage(e
                    .getMessage());
        }
    }

    private void sendCapturedVideo(){
        try {
            Cursor d = ServalD.rhizomeList(RhizomeManifest_File.SERVICE, null, null, null);
            FilteredCursor fc = new FilteredCursor(d);
            SubscriberId sid = ServalBatPhoneApplication.context.server.getIdentity().sid;
            for (int i = 0; i < fc.getCount(); i++) {
                fc.moveToNext();
                if (fc.getString(fc.getColumnIndex("name")).equals(sid.toString() + ".mp4")) {
                    BundleId bid = new BundleId(fc.getBlob(fc.getColumnIndex("id")));
                    Rhizome.unshareFile(bid);
                    Log.d(TAG, "File Unshared");
                }
            }

            File capturedVideo = new File(Environment.getExternalStorageDirectory() + File.separator
                    + Environment.DIRECTORY_DCIM + File.separator + sid.toString() + ".mp4");
            ServalDCommand.rhizomeAddFile(capturedVideo, null, null, sid, null);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
