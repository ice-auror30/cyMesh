package org.servalproject.protocol;

import org.servalproject.servaldna.AbstractId;
import org.servalproject.servaldna.AbstractMdpProtocol;
import org.servalproject.servaldna.AsyncResult;
import org.servalproject.servaldna.ChannelSelector;
import org.servalproject.servaldna.MdpPacket;
import org.servalproject.servaldna.SubscriberId;

import java.io.IOException;

public class CommandsProtocol extends AbstractMdpProtocol<CommandsProtocol.ProtocolResult> {
    public static final int PROTO_PORT = 8152;

    public static final byte MSG_PING = (byte) 0x01;
    public static final byte MSG_PONG = (byte) 0x02;
    public static final byte MSG_DATA = (byte) 0x03;

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

    public void ping(SubscriberId destination) throws IOException {
        MdpPacket ping = new MdpPacket();
        if (destination.isBroadcast())
            ping.setFlags(MdpPacket.MDP_FLAG_NO_CRYPT);
        ping.setRemoteSid(destination);
        ping.setRemotePort(PROTO_PORT);
        ping.payload.put(MSG_PING);
        socket.send(ping);
    }

    public void pong(SubscriberId destination) throws IOException {
        MdpPacket pong = new MdpPacket();
        pong.setRemoteSid(destination);
        pong.setRemotePort(PROTO_PORT);
        pong.payload.put(MSG_PONG);
        socket.send(pong);
    }

    public void send(SubscriberId destination, byte[] data) throws IOException {
        MdpPacket pkt = new MdpPacket();
        if (destination.isBroadcast())
            pkt.setFlags(MdpPacket.MDP_FLAG_NO_CRYPT);
        pkt.setRemoteSid(destination);
        pkt.setRemotePort(PROTO_PORT);
        pkt.payload.put(MSG_DATA);
        socket.send(pkt);
    }

    @Override
    protected void parse(MdpPacket response) {
        try {
            byte type = response.payload.get();
            switch (type) {
                case MSG_PING:
                    pong(response.getRemoteSid());
                    results.result(null);
                    break;
                case MSG_PONG:
                    results.result(null);
                    break;
                case MSG_DATA:
                    byte[] res = new byte[100]; // TODO: This should be the actual packet size
                    response.payload.get(res);
                    ProtocolResult result = new ProtocolResult(response.getRemoteSid(), type, res);
                    results.result(result);
            }
        } catch (AbstractId.InvalidBinaryException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
