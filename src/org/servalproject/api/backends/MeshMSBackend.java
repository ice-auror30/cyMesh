package org.servalproject.api.backends;

import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.servald.Peer;
import org.servalproject.servaldna.BundleId;
import org.servalproject.servaldna.ServalDInterfaceException;

import java.io.IOException;

/**
 * Created by matt2 on 10/5/16.
 */
public class MeshMSBackend extends AbstractBackend {
    public static final String NAME = "meshms";

    public MeshMSBackend(ServalBatPhoneApplication app) throws ServalDInterfaceException, IOException {
        super(app);
    }

    public boolean sendBytes(Peer dst, byte[] data, boolean persist, BundleId bundle) {
        throw new UnsupportedOperationException("Cannot send bytes over MeshMS");
    }

    @Override
    public boolean addData(Peer dst, byte[] data, BundleId bundle) {
        throw new UnsupportedOperationException("Cannot add data with MeshMS");
    }

    @Override
    public boolean sendString(Peer dst, String data, boolean persist, BundleId bundle){
        try {
            app.server.getRestfulClient().meshmsSendMessage(ident.sid, dst.sid, data);
            return true;
        }
        catch (Exception e) {
            // TODO: Probably shouldn't just eat this
            return false;
        }
    }
}
