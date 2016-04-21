package org.servalproject.api.backends;

import org.servalproject.servald.Peer;

/**
 * Defines a backend for the NetworkAPI to use. IBackends are intended to
 * be used to provide the ability to switch out the underlying protocols
 * being used by the NetworkAPI. A backend should provide all necessary
 * methods to send generic data.
 */
public interface IBackend {
    /**
     * Send arbitrary bytes to another Peer. Protocol specific things
     * should be implemented on top of this method.
     *
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
    boolean sendString(Peer dst, String data);

    /**
     * Send MSG_PING to the specific peer.
     *
     * @param dst The peer to ping
     */
    void sendPing(Peer dst);

    /**
     * Send MSG_PONG to the specified peer.
     *
     * Should only be sent after receiving MSG_PING.
     *
     * @param dst The peer to pong
     */
    void sendPong(Peer dst);
}
