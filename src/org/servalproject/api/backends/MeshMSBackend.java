package org.servalproject.api.backends;

import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.servald.Peer;
import org.servalproject.servaldna.ServalDInterfaceException;

import java.io.IOException;

public class MeshMSBackend extends AbstractBackend {
    public static final String NAME = "meshms";

    public MeshMSBackend(ServalBatPhoneApplication app) throws ServalDInterfaceException, IOException {
        super(app);
    }

    public boolean sendBytes(Peer dst, byte[] data) {
        throw new UnsupportedOperationException("Cannot send bytes over MeshMS");
    }

    public boolean sendString(Peer dst, String data) {
        try {
            app.server.getRestfulClient().meshmsSendMessage(ident.sid, dst.sid, data);
            return true;
        } catch (Exception e) {
            // TODO: Probably shouldn't just eat this
            return false;
        }
    }

    public void sendPing(Peer dst) {
        try {
            app.server.getRestfulClient().meshmsSendMessage(ident.sid, dst.sid, "PING");
        } catch (Exception e) {
            // TODO: Do something with this
        }
    }

    public void sendPong(Peer dst) {
        try {
            app.server.getRestfulClient().meshmsSendMessage(ident.sid, dst.sid, "PONG");
        } catch (Exception e) {
            // TODO: Do something with this
        }
    }
}
