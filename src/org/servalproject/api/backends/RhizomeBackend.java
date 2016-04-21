package org.servalproject.api.backends;

import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.servald.Peer;
import org.servalproject.servaldna.ServalDCommand;
import org.servalproject.servaldna.ServalDInterfaceException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class RhizomeBackend extends AbstractBackend {
    public static final String NAME = "rhizome";

    public RhizomeBackend(ServalBatPhoneApplication app) throws ServalDInterfaceException, IOException {
        super(app);
    }

    public boolean sendBytes(Peer dst, byte[] data) {
        try {
            List<String> args = new LinkedList<String>();
            args.add("rhizome");
            args.add("add");
            args.add("file");
            args.add(dst.sid.toHex());
            args.add(null); // Payload Path
            args.add(null); // Manifest Path
            args.add(null); // Separator?

            return false;
        }
        catch (Exception e) {
            return false;
        }
    }

    public boolean sendString(Peer dst, String data) {
        return false;
    }

    public void sendPing(Peer dst) {
        throw new UnsupportedOperationException("Can't PING over Rhizome");
    }

    public void sendPong(Peer dst) {
        throw new UnsupportedOperationException("Can't PONG over Rhizome");
    }
}
