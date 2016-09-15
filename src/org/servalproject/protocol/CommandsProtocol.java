package org.servalproject.protocol;

import android.content.Context;
import android.content.Intent;

import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.servaldna.AbstractId;
import org.servalproject.servaldna.AbstractMdpProtocol;
import org.servalproject.servaldna.AsyncResult;
import org.servalproject.servaldna.ChannelSelector;
import org.servalproject.servaldna.MdpPacket;
import org.servalproject.servaldna.SubscriberId;

import java.io.IOException;

public class CommandsProtocol extends AbstractMdpProtocol<CommandsProtocol.ProtocolResult> {
    public static final int PROTO_PORT = 8152;

    public static final String MESH_CMD = "org.servalproject.batphone.meshcmd";
    public static final String MESH_REQ = MESH_CMD + ".request";
    public static final String MESH_RESP = MESH_CMD + ".response";

    public static final String CMD_DATA = "data";

    public static final byte MSG_REQ = (byte) 0x01;
    public static final byte MSG_RESP = (byte) 0x02;

    public static class ProtocolResult {
        public final SubscriberId subscriberId;
        public byte type;
        public byte[] payload;

        public ProtocolResult(SubscriberId subscriberId, byte pt, byte[] payload) {
            this.subscriberId = subscriberId;
            this.type = pt;
            this.payload = payload;
        }
    }

    public CommandsProtocol(ChannelSelector selector, int loopbackMdpPort, AsyncResult<ProtocolResult> results) throws IOException {
        super(selector, loopbackMdpPort, results);
    }

    public void send(SubscriberId destination, byte[] data, byte type) throws IOException {
        MdpPacket pkt = new MdpPacket();
        if (destination.isBroadcast())
            pkt.setFlags(MdpPacket.MDP_FLAG_NO_CRYPT);
        pkt.setRemoteSid(destination);
        pkt.setRemotePort(PROTO_PORT);
        pkt.payload.put(type);
        pkt.payload.putInt(data.length);
        pkt.payload.put(data);
        socket.send(pkt);
    }

    public void sendRequest(SubscriberId destination, byte[] data) throws IOException {
        send(destination, data, MSG_REQ);
    }

    public void sendResponse(SubscriberId destination, byte[] data) throws IOException {
        send(destination, data, MSG_RESP);
    }

    @Override
    protected void parse(MdpPacket response) {
        try {
            byte type = response.payload.get();
            int size = response.payload.getInt();
            byte[] res = new byte[size];
            Intent intent;
            ProtocolResult result;

            switch (type) {
                case MSG_REQ:
                    response.payload.get(res);

                    intent = new Intent();
                    intent.setAction(MESH_REQ);
                    intent.putExtra(CMD_DATA, res);

                    ServalBatPhoneApplication.context.sendBroadcast(intent);

                    result = new ProtocolResult(response.getRemoteSid(), type, res);
                    results.result(result);
                    break;
                case MSG_RESP:
                    response.payload.get(res);

                    intent = new Intent();
                    intent.setAction(MESH_RESP);
                    intent.putExtra(CMD_DATA, res);

                    ServalBatPhoneApplication.context.sendBroadcast(intent);

                    result = new ProtocolResult(response.getRemoteSid(), type, res);
                    results.result(result);
                    break;
                /*
                case MSG_DATA:
                    byte[] res = new byte[100]; // TODO: This should be the actual packet size
                    response.payload.get(res);
                    ProtocolResult result = new ProtocolResult(response.getRemoteSid(), type, res);
                    results.result(result);
                */
            }
        } catch (AbstractId.InvalidBinaryException e) {
            e.printStackTrace();
        }

    }
}
