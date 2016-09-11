package org.servalproject.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.IBinder;
import android.util.Log;

import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.rhizome.Rhizome;
import org.servalproject.rhizome.RhizomeManifest;
import org.servalproject.rhizome.RhizomeManifest_File;
import org.servalproject.servald.ServalD;
import org.servalproject.servaldna.ServalDCommand;
import org.servalproject.servaldna.SubscriberId;

import java.io.File;

public class CameraService extends Service {


    public static SubscriberId senderID;
    private RhizomeManifest mManifest;
    private File mManifestFile;
    private File mPayloadFile;
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

    public static void setSenderID(SubscriberId SID){

        senderID = SID;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Rhizome.ACTION_RECEIVE_FILE)) {
                Log.d("CameraService","RECEIVER ACTIVE");
                RhizomeManifest_File file = (RhizomeManifest_File)mManifest;
                try {
                    Cursor c = ServalD.rhizomeList(RhizomeManifest_File.SERVICE, null, senderID, null);
                    if(c.getCount() > 0){
                        DatabaseUtils.dumpCursorToString(c);
                    }
                    mPayloadFile = Rhizome.savedPayloadFileFromName(file.getName());
                    mManifestFile = Rhizome.savedManifestFileFromName(file.getName());

                    ServalDCommand.rhizomeExtractBundle(file.getManifestId(), mManifestFile, mPayloadFile);

                    File savedDir = Rhizome.getSaveDirectoryCreated();
                    Log.d("CameraService","SavedDir:" + savedDir.getName());
                    if (savedDir.isDirectory()) {
                        String[] filenames = savedDir.list();
                        for (String filename : filenames) {
                            if (filename.startsWith(".manifest.") && filename.length() > 10) {
                                File mPayloadFile = new File(savedDir, filename.substring(10));
                                if (mPayloadFile.isFile()) {
//                                        if (mPayloadFile.getName().contains(requestedSID)) {
//                                            Uri uri;
//                                            if (mPayloadFile != null && mPayloadFile.exists()) {
//                                                String ext = mPayloadFile.getName().substring(mPayloadFile.getName().lastIndexOf(".") + 1);
//                                                String contentType = MimeTypeMap.getSingleton()
//                                                        .getMimeTypeFromExtension(ext);
//                                                uri = Uri.fromFile(mPayloadFile);
//                                                Log.i(Rhizome.TAG, "Open uri='" + uri + "', contentType='" + contentType + "'");
//                                                Intent newIntent = new Intent();
//                                                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                                newIntent.setAction(Intent.ACTION_VIEW);
//                                                newIntent.setDataAndType(uri, contentType);
//                                                getApplicationContext().startActivity(newIntent);
//                                            }
//                                        }
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
