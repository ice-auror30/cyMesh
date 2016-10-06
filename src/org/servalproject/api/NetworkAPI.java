package org.servalproject.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.api.backends.IBackend;
import org.servalproject.api.backends.MeshMSBackend;
import org.servalproject.api.backends.RhizomeBackend;
import org.servalproject.protocol.CommandsProtocol;
import org.servalproject.rhizome.MeshMS;
import org.servalproject.servald.Peer;
import org.servalproject.servaldna.AsyncResult;
import org.servalproject.servaldna.ServalDCommand;
import org.servalproject.servaldna.ServalDInterfaceException;
import org.servalproject.servaldna.SubscriberId;
import org.servalproject.servaldna.keyring.KeyringIdentity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The entry point into the network api for sending messages across
 * serval mesh. The caller of this API does not need to worry about
 * how the data/messages are being sent, that is the job of the API.
 */
public class NetworkAPI {
    public static final String TAG = NetworkAPI.class.getName();
    private static NetworkAPI instance;
    private static ServalBatPhoneApplication app;

    private ArrayList<IMeshListener> meshListeners;
    private HashMap<String, IBackend> backends = new HashMap<String, IBackend>();

    private CommandsProtocol commandSocket = null;

    public static final String MESH_CMD = "org.servalproject.batphone.meshcmd";
    public static final String MESH_REQ = MESH_CMD + ".request";
    public static final String MESH_RESP = MESH_CMD + ".response";
    public static final String MESH_START = MESH_CMD + ".start";
    public static final String MESH_END = MESH_CMD + ".end";

    public static final String CMD_DATA = "data";
    public static final String CMD_SRC = "source";

    private NetworkAPI() {
        app = ServalBatPhoneApplication.context;
        meshListeners = new ArrayList<IMeshListener>();

        initBackends();
        initCommands();
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

    private void initBackends() {
        try {
            backends.put(RhizomeBackend.NAME, new RhizomeBackend(app));
            backends.put(MeshMSBackend.NAME, new MeshMSBackend(app));
        }
        catch (Exception e) {
            // TODO: Get it done, then make it "clean"
        }
    }

    private void initCommands() {
        try {
            if (commandSocket == null) {
                commandSocket = app.server.getCommandProtocol(new AsyncResult<CommandsProtocol.ProtocolResult>() {
                    @Override
                    public void result(CommandsProtocol.ProtocolResult nextResult) {
                        final byte type = nextResult.type;
                        final byte[] payload = nextResult.payload;
                        final SubscriberId sid = nextResult.subscriberId;
                        Log.d(TAG, "Got message of type " + type);

                        Intent intent = new Intent();

                        switch (type) {
                            case CommandsProtocol.MSG_REQ:
                                Log.d(TAG, "Message was a Request");
                                intent.setAction(MESH_REQ);
                                intent.putExtra(CMD_SRC, sid.toHex());
                                intent.putExtra(CMD_DATA, payload);

                                app.sendBroadcast(intent);
                                break;
                            case CommandsProtocol.MSG_RESP:
                                Log.d(TAG, "Message was a Response");
                                intent.setAction(MESH_RESP);
                                intent.putExtra(CMD_SRC, sid.toHex());
                                intent.putExtra(CMD_DATA, payload);

                                app.sendBroadcast(intent);
                                break;
                            case CommandsProtocol.MSG_START:
                                Log.d(TAG, "Message was a Start Command");
                                intent.setAction(MESH_START);
                                intent.putExtra(CMD_SRC, sid.toHex());
                                intent.putExtra(CMD_DATA, payload);

                                app.sendBroadcast(intent);
                                break;
                            case CommandsProtocol.MSG_END:
                                Log.d(TAG, "Message was an End Command");
                                intent.setAction(MESH_END);
                                intent.putExtra(CMD_SRC, sid.toHex());
                                intent.putExtra(CMD_DATA, payload);

                                app.sendBroadcast(intent);
                                break;
                        }

                        app.sendBroadcast(intent);
                    }
                });
            }

        } catch (ServalDInterfaceException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public boolean sendString(Peer peer, String message) {
        return backends.get(MeshMSBackend.NAME).sendString(peer, message, false, null);
    }

    /**
     * Register a callback to be called when a mew message is received.
     */
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

    /**
     * WIP Implementation of sending a file.
     *
     * @param toSend The path of the file to send
     * @return true on success, false otherwise
     */
    public boolean sendFile(File toSend) {
        return sendFile(toSend, null);
    }

    /**
     * WIP Implementation of sending a file.
     *
     * Note that there is no peer to send to, this is because
     * rhizome puts the file on all devices in the mesh. We may
     * want to accept the peer id here in case we don't use
     * rhizome.
     *
     * @param toSend The path of the file to send
     * @param manifest The path of the manifest
     * @return true on success, false otherwise
     */
    public boolean sendFile(File toSend, File manifest) {
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

    public void sendCommand(SubscriberId dst, byte[] data, CommandType type) {
        if (commandSocket == null) {
            initCommands();
        }
        try {
            commandSocket.send(dst, data, type.value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRequest(SubscriberId dst, byte[] data) {
        sendCommand(dst, data, CommandType.REQUEST);
    }

    public void sendResponse(SubscriberId dst, byte[] data) {
        sendCommand(dst, data, CommandType.RESPONSE);
    }

    /////////////////////////
    // Broadcast Receivers //
    /////////////////////////
    private BroadcastReceiver meshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MeshMS.NEW_MESSAGES)) {
                notifyMeshListeners();
            }
        }
    };

    public enum CommandType {
        REQUEST(CommandsProtocol.MSG_REQ),
        RESPONSE(CommandsProtocol.MSG_RESP),
        START(CommandsProtocol.MSG_START),
        END(CommandsProtocol.MSG_END);

        public byte value;

        CommandType(byte value) {
            this.value = value;
        }
    }
}
