package org.servalproject.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.rhizome.Rhizome;

import java.io.File;
import java.util.LinkedList;

public class CameraService extends Service {


    //private Handler handler;
    //private Cursor c;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
//        try {
//            c = ServalD.rhizomeList(RhizomeManifest_File.SERVICE, null, null, null);
//        } catch (Exception e) {
//            Log.e("RhizomeList", e.getMessage(), e);
//            ServalBatPhoneApplication.context.displayToastMessage(e
//                    .getMessage());
//        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Rhizome.ACTION_RECEIVE_FILE);
        try {
            filter.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Log.e("RhizomeList", e.toString(), e);
        }
        this.registerReceiver(receiver, filter, Rhizome.RECEIVE_PERMISSION,
                null);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Rhizome.ACTION_RECEIVE_FILE)) {
                Log.d("CameraService","RECEIVER ACTIVE");
                try {
                    File savedDir = Rhizome.getSaveDirectory(); //doesn't work currently
                    Log.d("CameraServce","SavedDir:" + savedDir.getName());
                    LinkedList<String> names = new LinkedList<String>();
                    if (savedDir.isDirectory()) {
                        String[] filenames = savedDir.list();
                        for (String filename : filenames) {
                            if (filename.startsWith(".manifest.") && filename.length() > 10) {
                                File payloadfile = new File(savedDir, filename.substring(10));
                                if (payloadfile.isFile()) {
                                    if (payloadfile.getName().contains("195435")) {
                                        Uri uri;
                                        if (payloadfile != null && payloadfile.exists()) {
                                            String ext = payloadfile.getName().substring(payloadfile.getName().lastIndexOf(".") + 1);
                                            String contentType = MimeTypeMap.getSingleton()
                                                    .getMimeTypeFromExtension(ext);
                                            uri = Uri.fromFile(payloadfile);
                                            Log.i(Rhizome.TAG, "Open uri='" + uri + "', contentType='" + contentType + "'");
                                            Intent newIntent = new Intent();
                                            newIntent.setAction(Intent.ACTION_VIEW);
                                            newIntent.setDataAndType(uri, contentType);
                                            getApplicationContext().startActivity(newIntent);
                                        }
                                        names.add(payloadfile.getName());
                                    }
                                }
                            }
                        }
                    }else{
                        Log.d("CameraService","Not a directory");
                    }
                } catch (Exception e) {
                    Log.e(Rhizome.TAG, e.toString(), e);
                    ServalBatPhoneApplication.context.displayToastMessage(e
                            .getMessage());
                }
            }
        }
    };
}
