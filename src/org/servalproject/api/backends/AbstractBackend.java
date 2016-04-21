package org.servalproject.api.backends;

import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.servaldna.ServalDInterfaceException;
import org.servalproject.servaldna.keyring.KeyringIdentity;

import java.io.IOException;

/**
 * Created by matt2 on 4/20/16.
 */
public abstract class AbstractBackend implements IBackend {
    ServalBatPhoneApplication app;
    KeyringIdentity ident;

    public AbstractBackend(ServalBatPhoneApplication app) throws ServalDInterfaceException, IOException {
        this.app = app;
        this.ident = app.server.getIdentity();
    }
}
