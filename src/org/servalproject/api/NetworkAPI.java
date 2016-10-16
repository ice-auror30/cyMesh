package org.servalproject.api;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.protocol.CommandsProtocol;
import org.servalproject.servaldna.AsyncResult;
import org.servalproject.servaldna.ServalDCommand;
import org.servalproject.servaldna.ServalDFailureException;
import org.servalproject.servaldna.ServalDInterfaceException;
import org.servalproject.servaldna.SubscriberId;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The entry point into the network api for sending messages across
 * serval mesh. The caller of this API does not need to worry about
 * how the data/messages are being sent, that is the job of the API.
 */
public class NetworkAPI {
    public static final String TAG = NetworkAPI.class.getName();
    private static NetworkAPI instance;
    private static ServalBatPhoneApplication app;

    private CommandsProtocol commandSocket = null;

    public static final String MESH_CMD = "org.servalproject.batphone.meshcmd";
    public static final String MESH_REQ = MESH_CMD + ".request";
    public static final String MESH_RESP = MESH_CMD + ".response";
    public static final String MESH_START = MESH_CMD + ".start";
    public static final String MESH_END = MESH_CMD + ".end";
    public static final String MESH_TRANSFER = MESH_CMD + ".transfer";

    public static final String CMD_DATA = "data";
    public static final String CMD_SRC = "source";
    public static final String CMD_COMMAND = "cmd";

    public static final int TCP_TRANSFER_PORT = 8193;
    public static final int MDP_TRANSFER_PORT = 83;

    private NetworkAPI() {
        app = ServalBatPhoneApplication.context;
    }

    public void initCommands() {
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

                                Command cmd = Command.parse(payload);
                                if (cmd.getAction().equals(MESH_TRANSFER)) {
                                    receiveFile(sid, cmd);
                                }

                                intent.putExtra(CMD_COMMAND, cmd.asBundle());

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

    public static NetworkAPI getInstance() {
        if (instance == null) {
            instance = new NetworkAPI();
        }

        return instance;
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

    public void sendStart(SubscriberId dst, Command cmd) {
        sendCommand(dst, cmd.asBytes(), CommandType.START);
    }

    public void sendEnd(SubscriberId dst, String action) {
        sendCommand(dst, action.getBytes(), CommandType.END);
    }

    public boolean sendFile(SubscriberId dst, File file) {
        Command cmd = new Command(MESH_TRANSFER);
        cmd.putExtra("filename", file.getName());
        cmd.putExtra("length", (int)file.length());
        return sendFile(dst, file, cmd);
    }

    public boolean sendFile(SubscriberId dst, File file, Command cmd) {
        try {
            Log.i(TAG, "Sending File");

            ServerSocket ss = new ServerSocket(TCP_TRANSFER_PORT);

            Log.d(TAG, "Creating MSP Tunnel");
            MspListener listener = new MspListener();
            listener.execute();

            Log.d(TAG, "Accepting tunnel socket");
            Socket tunneledSocket = ss.accept();
            InputStream inStream = tunneledSocket.getInputStream();

            Log.d(TAG, "Sending START Command");
            sendStart(dst, cmd);

            Log.d(TAG, "Waiting for RDY command");
            // Wait for the receiving socket to be ready
            byte[] rdy = new byte[3];
            int read = inStream.read(rdy, 0, 3);
            if (read != 3 || !new String(rdy).toUpperCase().equals("RDY")) {
                Log.e(TAG, "Got bad response from transfer client. Try again");

                inStream.close();
                tunneledSocket.close();
                return false;
            }

            // Transfer the file
            Log.d(TAG, "Starting file transfer");
            OutputStream outStream = tunneledSocket.getOutputStream();
            byte[] buffer = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            // Read the file contents and put them into the socket
            bis.read(buffer,0,buffer.length);
            outStream.write(buffer,0,buffer.length);
            outStream.flush();

            bis.close();
            fis.close();
            tunneledSocket.close();
            sendEnd(dst, MESH_TRANSFER);
            Log.i(TAG, "Finished File Transfer");

            listener.cancel(true);

            if (!ss.isClosed())
                ss.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void receiveFile(SubscriberId frm, Command cmd) {
        try {
            Log.i(TAG, "Receiving File");
            Log.d(TAG, "Opening Tunnel");
            MspConnector connector = new MspConnector();
            connector.execute(frm);

            Socket tunnelSocket = new Socket("localhost", TCP_TRANSFER_PORT);
            tunnelSocket.getOutputStream().write("RDY".getBytes());

            InputStream inStream = tunnelSocket.getInputStream();
            byte[] buffer = new byte[cmd.getExtraInt("length")];
            File file = new File(cmd.getExtraString("filename"));
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            inStream.read(buffer, 0, buffer.length);
            bos.write(buffer,0,buffer.length);
            bos.flush();

            bos.close();
            fos.close();
            inStream.close();
            tunnelSocket.close();
            Log.i(TAG, "Finished File Transfer");

            connector.cancel(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    // Dirty, Dirty Hacks
    private class MspListener extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... v) {
            try {
                ServalDCommand.mspTunnnelCreate(TCP_TRANSFER_PORT, MDP_TRANSFER_PORT);
            } catch (ServalDFailureException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    class MspConnector extends AsyncTask<SubscriberId, Void, Void> {
        protected Void doInBackground(SubscriberId... s) {
            if (s.length != 1) {
                throw new IllegalArgumentException("Needs 1 subscriber id");
            }

            try {
                ServalDCommand.mspTunnelConnect(TCP_TRANSFER_PORT, s[0], MDP_TRANSFER_PORT);
            } catch (ServalDFailureException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
