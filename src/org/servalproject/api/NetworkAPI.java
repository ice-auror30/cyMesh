package org.servalproject.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.rhizome.MeshMS;
import org.servalproject.servald.Peer;
import org.servalproject.servaldna.ServalDCommand;
import org.servalproject.servaldna.ServalDInterfaceException;
import org.servalproject.servaldna.keyring.KeyringIdentity;
import org.servalproject.servaldna.meshms.MeshMSException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * The entry point into the network api for sending messages across
 * serval mesh. The caller of this API does not need to worry about
 * how the data/messages are being sent, that is the job of the API.
 */
public class NetworkAPI {
    private static NetworkAPI instance;
    private static ServalBatPhoneApplication app;

    private ArrayList<IMeshListener> meshListeners;

    private NetworkAPI() {
        app = ServalBatPhoneApplication.context;
        meshListeners = new ArrayList<IMeshListener>();

        registerReceivers();
    }

    /**
     * Call to register the APIs broadcast receivers. This is done
     * automatically by the constructor but needs to be done again
     * if unregisterReceivers is called.
     */
    public void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MeshMS.NEW_MESSAGES);
        app.registerReceiver(meshReceiver, filter);
    }

    /**
     * Unregister the API's broadcast receivers.
     */
    public void unregisterReceivers() {
        app.unregisterReceiver(meshReceiver);
    }

    public static NetworkAPI getInstance() {
        if (instance == null) {
            instance = new NetworkAPI();
        }

        return instance;
    }

    ////////////////////
    // MeshMS Methods //
    ////////////////////

    /**
     * Send a MeshMS message to the specified peer
     *
     * @param peer The peer to send to
     * @param message The message to send
     */
    public boolean sendMeshMS(Peer peer, String message) {
        try {
            KeyringIdentity identity = app.server.getIdentity();
            app.server.getRestfulClient().meshmsSendMessage(identity.sid, peer.sid, message);
            return true;
        } catch (ServalDInterfaceException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (MeshMSException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void registerMeshListener(IMeshListener l) {
        meshListeners.add(l);
    }

    public void unregisterMeshListener(IMeshListener l) {
        meshListeners.remove(l);
    }

    protected void notifyMeshListeners() {
        for (IMeshListener l : meshListeners) {
            l.onMeshMSMessage();
        }
    }

    /////////////
    // Rhizome //
    /////////////
    public boolean sendFile(Peer to, File toSend) {
        return sendFile(to, toSend, null);
    }

    public boolean sendFile(Peer to, File toSend, File manifest) {
        try {
            KeyringIdentity identity = app.server.getIdentity();
            ServalDCommand.rhizomeAddFile(toSend, manifest, null, identity.sid, null);
            return true;
        } catch (ServalDInterfaceException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /////////////////////////
    // Broadcast Receivers //
    /////////////////////////
    BroadcastReceiver meshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MeshMS.NEW_MESSAGES)) {
                notifyMeshListeners();
            }
        }
    };
}
