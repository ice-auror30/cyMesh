package org.servalproject.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.servalproject.api.NetworkAPI;

public class NetworkService extends Service {
    public static final String TAG = NetworkService.class.getName();
    private NetworkAPI api;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        api = NetworkAPI.getInstance();
        api.registerReceivers();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NetworkAPI.MESH_REQ);
        intentFilter.addAction(NetworkAPI.MESH_RESP);

        this.registerReceiver(pingReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        api.unregisterReceivers();
        this.unregisterReceiver(pingReceiver);
    }

    BroadcastReceiver pingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(NetworkAPI.MESH_REQ)) {
                byte[] cmd = intent.getByteArrayExtra(NetworkAPI.CMD_DATA);
                if (new String(cmd).equals("PING")) {
                    Log.i(TAG, "Received PING");
                    Toast.makeText(NetworkService.this, "Received PING", Toast.LENGTH_LONG).show();
                }
            }
        }
    };
}
