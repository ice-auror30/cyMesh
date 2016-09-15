package org.servalproject.api.backends;

import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.servald.Peer;
import org.servalproject.servaldna.BundleId;
import org.servalproject.servaldna.ServalDInterfaceException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class RhizomeBackend extends AbstractBackend {
    public static final String NAME = "rhizome";

    public RhizomeBackend(ServalBatPhoneApplication app) throws ServalDInterfaceException, IOException {
        super(app);
    }

    public boolean sendBytes(Peer dst, byte[] data, boolean persist, BundleId bundle) {
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

    public boolean sendBytes(Peer dst, byte[] data) {
        return sendBytes(dst, data, false, null);
    }

    public boolean sendString(Peer dst, String data) {
        return false;
    }

    public boolean sendString(Peer dst, String data, boolean persist, BundleId bundle) {
        return sendString(dst, data);
    }

    @Override
    public boolean addData(Peer dst, byte[] data, BundleId bundle) {
        return false;
    }
}
