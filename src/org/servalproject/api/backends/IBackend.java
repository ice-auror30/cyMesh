package org.servalproject.api.backends;

import org.servalproject.servald.Peer;
import org.servalproject.servaldna.BundleId;

/**
 * Defines a backend for the NetworkAPI to use. IBackends are intended to
 * be used to provide the ability to switch out the underlying protocols
 * being used by the NetworkAPI. A backend should provide all necessary
 * methods to send generic data.
 *
 * Needs the following features:
 *
 *  - Send Bytes
 *  - Commands
 *  - Persistent Messages (Updates to Rhizome Files)
 *  - Rhizome Journals
 */
public interface IBackend {
    /**
     * Send arbitrary bytes to another Peer. Protocol specific things
     * should be implemented on top of this method.
     *
     * @param dst The peer to send to
     * @param data The data to send
     * @param persist When using Rhizome, update an already existing file
     * @param bundle The bundle id to update if persist is true, meaningless otherwise
     * @return true if successful, false otherwise
     */
    boolean sendBytes(Peer dst, byte[] data, boolean persist, BundleId bundle);

    /**
     * Send arbitrary bytes to another Peer.
     * @param dst The peer to send to
     * @param data The data to send
     * @return true if successful, false otherwise
     */
    boolean sendBytes(Peer dst, byte[] data);

    /**
     * Send string to another Peer. This is intended to be used for
     * protocols that do not support sending bytes (MeshMS). In other
     * protocols, this should ultimately use sendBytes.
     *
     * @param dst The peer to send to
     * @param data The data to send
     * @return true if successful, false otherwise
     */
    boolean sendString(Peer dst, String data, boolean persist, BundleId bundle);

    /**
     * Append data to an existing location (i.e, Rhizome Journal).
     *
     * @param dst The peer to send to
     * @param data The data to send
     * @param bundle The existing location
     * @return true if successful, false otherwise
     */
    boolean addData(Peer dst, byte[] data, BundleId bundle);
}
