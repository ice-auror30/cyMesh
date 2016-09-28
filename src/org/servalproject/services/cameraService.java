package org.servalproject.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.rhizome.FilteredCursor;
import org.servalproject.rhizome.Rhizome;
import org.servalproject.rhizome.RhizomeManifest;
import org.servalproject.rhizome.RhizomeManifest_File;
import org.servalproject.servald.ServalD;
import org.servalproject.servaldna.BundleId;
import org.servalproject.servaldna.ServalDCommand;
import org.servalproject.servaldna.SubscriberId;

import java.io.File;

public class CameraService extends Service {


    public static SubscriberId senderID;
    private static String TAG = "CameraService";

    private RhizomeManifest mManifest;
    private File mManifestFile;
    private File mPayloadFile;
    private IntentFilter filter;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        filter = new IntentFilter();
        filter.addAction(Rhizome.ACTION_RECEIVE_FILE);
        try {
            filter.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Log.e("RhizomeList", e.toString(), e);
        }
        this.registerReceiver(receiver, filter, Rhizome.RECEIVE_PERMISSION,
                null);
    }

    public void onDestroy(){
        this.registerReceiver(receiver, filter, Rhizome.RECEIVE_PERMISSION,
                null);
    }

    public static void setSenderID(SubscriberId SID){

        senderID = SID;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Rhizome.ACTION_RECEIVE_FILE)) {
                Log.d(TAG,"intent.toString():" + intent.toString());
                Log.d(TAG,"intent.getStringExtra():" + intent.getStringExtra("typ"));
                Bundle b = intent.getExtras();
                Log.d(TAG,"b.getString(\"typ\"):" + b.getString("typ"));


                if (senderID != null) {
                    try {
                        Cursor d = ServalD.rhizomeList(RhizomeManifest_File.SERVICE, null, null, null);
                        FilteredCursor fc = new FilteredCursor(d);
                        for (int i = 0; i < fc.getCount(); i++) {
                            fc.moveToNext();
                            if (fc.getString(fc.getColumnIndex("name")).equals(senderID.toString() + ".mp4")) {
                                BundleId bid = new BundleId(fc.getBlob(fc.getColumnIndex("id")));

                                File dir = Rhizome.getTempDirectoryCreated();

                                final File temp = new File(dir, bid.toHex() + ".mp4");
                                temp.delete();
                                ServalDCommand.rhizomeExtractFile(bid, temp);

                                String ext = temp.getName().substring(temp.getName().lastIndexOf(".") + 1);
                                String contentType = MimeTypeMap.getSingleton()
                                        .getMimeTypeFromExtension(ext);
                                Uri uri = Uri.fromFile(temp);
                                Log.i(Rhizome.TAG, "Open uri='" + uri + "', contentType='" + contentType + "'");
                                Intent newIntent = new Intent();
                                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                newIntent.setAction(Intent.ACTION_VIEW);
                                newIntent.setDataAndType(uri, contentType);
                                getApplicationContext().startActivity(newIntent);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(Rhizome.TAG, e.toString(), e);
                        ServalBatPhoneApplication.context.displayToastMessage(e
                                .getMessage());
                    }
                }
            }
        }
    };
}
