/**
 * Copyright (C) 2011 The Serval Project
 *
 * This file is part of Serval Software (http://www.servalproject.org)
 *
 * Serval Software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.servalproject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.servalproject.ServalBatPhoneApplication.State;
import org.servalproject.batphone.CallDirector;
import org.servalproject.rhizome.FilteredCursor;
import org.servalproject.rhizome.MeshMS;
import org.servalproject.rhizome.Rhizome;
import org.servalproject.rhizome.RhizomeMain;
import org.servalproject.rhizome.RhizomeManifest_File;
import org.servalproject.sensors.RecordClick;
import org.servalproject.servald.PeerListService;
import org.servalproject.servald.ServalD;
import org.servalproject.servaldna.BundleId;
import org.servalproject.servaldna.ServalDCommand;
import org.servalproject.servaldna.keyring.KeyringIdentity;
import org.servalproject.services.CameraService;
import org.servalproject.ui.Networks;
import org.servalproject.ui.ShareUsActivity;
import org.servalproject.ui.help.HtmlHelp;
import org.servalproject.wizard.Wizard;

import java.io.File;

/**
 *
 * Main activity which presents the Serval launcher style screen. On the first
 * time Serval is installed, this activity ensures that a warning dialog is
 * presented and the user is taken through the setup wizard. Once setup has been
 * confirmed the user is taken to the main screen.
 *
 * @author Paul Gardner-Stephen <paul@servalproject.org>
 * @author Andrew Bettison <andrew@servalproject.org>
 * @author Corey Wallis <corey@servalproject.org>
 * @author Jeremy Lakeman <jeremy@servalproject.org>
 * @author Romana Challans <romana@servalproject.org>
 */
public class Main extends Activity implements OnClickListener{
    public ServalBatPhoneApplication app;
    private static final String TAG = "Main";
    private TextView buttonToggle;
    private ImageView buttonToggleImg;
    private Drawable powerOnDrawable;
    private Drawable powerOffDrawable;
    public static SurfaceView dummySurface;
    public static RecordClick rc = null;
    private static boolean firstLoad = false;
    private static RelativeLayout lm = null;
    static Context c;
    private String sidstring;

    private void openMaps() {
        // check to see if maps is installed
        try {
            PackageManager mManager = getPackageManager();
            mManager.getApplicationInfo("org.servalproject.maps",
                    PackageManager.GET_META_DATA);

            Intent mIntent = mManager
                    .getLaunchIntentForPackage("org.servalproject.maps");
            mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(mIntent);

        } catch (NameNotFoundException e) {
            startActivity(new Intent(getApplicationContext(),
                    org.servalproject.ui.MapsActivity.class));
        }
    }

    @Override
    public void onClick(View view) {
        // Do nothing until upgrade finished.
        if (app.getState() != State.Running)
            return;

        switch (view.getId()){
            case R.id.btncall:
                if (!PeerListService.havePeers()) {
                    app.displayToastMessage("You do not have a connection to any other phones");
                    return;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_DIAL));
                    return;
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                startActivity(new Intent(app, CallDirector.class));
                break;
            case R.id.messageLabel:
                if (!ServalD.isRhizomeEnabled()) {
                    app.displayToastMessage("Messaging cannot function without an sdcard");
                    return;
                }
                startActivity(new Intent(getApplicationContext(),
                        org.servalproject.messages.MessagesListActivity.class));
                break;
            case R.id.mapsLabel:
                openMaps();
                break;
            case R.id.contactsLabel:
                startActivity(new Intent(getApplicationContext(),
                        org.servalproject.ui.ContactsActivity.class));
                break;
            case R.id.settingsLabel:
                startActivity(new Intent(getApplicationContext(),
                        org.servalproject.ui.SettingsScreenActivity.class));
                break;
            case R.id.sharingLabel:
                startActivity(new Intent(getApplicationContext(),
                        RhizomeMain.class));
                break;
            case R.id.helpLabel:
                Intent intent = new Intent(getApplicationContext(),
                        HtmlHelp.class);
                intent.putExtra("page", "helpindex.html");
                startActivity(intent);
                break;
            case R.id.servalLabel:
                startActivity(new Intent(getApplicationContext(),
                        ShareUsActivity.class));
                break;
            case R.id.powerLabel:
                startActivity(new Intent(getApplicationContext(),
                        Networks.class));
                break;
            case R.id.sharingSensorLabel:
                startActivity(new Intent(getApplicationContext(),
                        org.servalproject.rhizome.ShareSensorActivity.class));
                break;
            case R.id.getCameraLabel:

                break;
            case R.id.visualLabel:
                startActivity(new Intent(getApplicationContext(),
                        org.servalproject.VisualizationActivity.class));
                break;
        }
    }

    /*	public static SurfaceView addSVtoMain(){
            SurfaceView sv = new SurfaceView(c);
            lm.addView(sv);
            sv.setVisibility(View.INVISIBLE);
            return sv;
        }*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c = this;
        this.app = (ServalBatPhoneApplication) this.getApplication();
        setContentView(R.layout.main);

        dummySurface = (SurfaceView) findViewById(R.id.cameraView);

        // adjust the power button label on startup
        buttonToggle = (TextView) findViewById(R.id.btntoggle);
        buttonToggleImg = (ImageView) findViewById(R.id.powerLabel);
        buttonToggleImg.setOnClickListener(this);

        // load the power drawables
        powerOnDrawable = getResources().getDrawable(
                R.drawable.ic_launcher_power);
        powerOffDrawable = getResources().getDrawable(
                R.drawable.ic_launcher_power_off);

        int listenTo[] = {
                R.id.btncall,
                R.id.messageLabel,
                R.id.mapsLabel,
                R.id.contactsLabel,
                R.id.settingsLabel,
                R.id.sharingLabel,
                R.id.helpLabel,
                R.id.servalLabel,
                R.id.sharingSensorLabel,
                R.id.getCameraLabel,
                R.id.visualLabel
        };
        for (int i = 0; i < listenTo.length; i++) {
            this.findViewById(listenTo[i]).setOnClickListener(this);
        }

        startService(new Intent(this, CameraService.class));
        rc = new RecordClick(app);
        try {
            sidstring = app.server.getIdentity().sid.toString();
        }catch(Exception E){
            E.printStackTrace();
            Log.d(TAG, "Failed to set SIDstring in Main for Camera");
        }

    }

    public static SurfaceView getCameraSurface(){
        return dummySurface;
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(MeshMS.NEW_MESSAGES)) {
                rc.onClick(sidstring);
            }
            if(intent.getAction().equals(ServalBatPhoneApplication.ACTION_STATE)) {
                int stateOrd = intent.getIntExtra(
                        ServalBatPhoneApplication.EXTRA_STATE, 0);
                State state = State.values()[stateOrd];
                stateChanged(state);
            }
            if(intent.getAction().equals(RecordClick.RECORDING_FINISHED)) {
                sendCapturedVideo();
            }
        }
    };

    boolean registered = false;

    private void stateChanged(State state) {
        switch (state){
            case Running: case Upgrading: case Starting:
                // change the image for the power button
                buttonToggleImg.setImageDrawable(
                        app.isEnabled()?powerOnDrawable:powerOffDrawable);

                TextView pn = (TextView) this.findViewById(R.id.mainphonenumber);
                String id = this.getString(state.getResourceId());
                if (state == State.Running) {
                    try {
                        KeyringIdentity identity = app.server.getIdentity();

                        if (identity.did != null)
                            id = identity.did;
                        else
                            id = identity.sid.abbreviation();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
                pn.setText(id);
                break;
            case RequireDidName: case NotInstalled: case Installing:
                this.startActivity(new Intent(this, Wizard.class));
                finish();
                app.startBackgroundInstall();
                break;
            case Broken:
                // TODO display error?
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!registered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ServalBatPhoneApplication.ACTION_STATE);
            filter.addAction(MeshMS.NEW_MESSAGES);
            filter.addAction(RecordClick.RECORDING_FINISHED);
            this.registerReceiver(receiver, filter);
            registered = true;
        }

        dummySurface.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (registered) {
            this.unregisterReceiver(receiver);
            registered = false;
        }
        dummySurface.setVisibility(View.INVISIBLE);
    }

    private void sendCapturedVideo(){
        try {
            Cursor d = ServalD.rhizomeList(RhizomeManifest_File.SERVICE, null, null, null);
            FilteredCursor fc = new FilteredCursor(d);
            KeyringIdentity identity = ServalBatPhoneApplication.context.server.getIdentity();
            for (int i = 0; i < fc.getCount(); i++) {
                fc.moveToNext();
                if (fc.getString(fc.getColumnIndex("name")).equals(identity.sid.toString() + ".mp4")) {
                    BundleId bid = new BundleId(fc.getBlob(fc.getColumnIndex("id")));
                    Rhizome.unshareFile(bid);
                    Log.d(TAG, "File Unshared");
                }
                Log.d(TAG, fc.getString(fc.getColumnIndex("name")));
            }

            File capturedVideo = new File(Environment.getExternalStorageDirectory() + File.separator
                    + Environment.DIRECTORY_DCIM + File.separator + identity.sid.toString() + ".mp4");
            ServalDCommand.rhizomeAddFile(capturedVideo, null, null, identity.sid, null);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
