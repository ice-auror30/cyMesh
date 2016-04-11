package org.servalproject.api;

public interface IMeshListener {
    /**
     * Notify the listener about any new messages. MeshMS does
     * not include any additonal details about the new message
     * without hitting the rest server again.
     *
     * @todo Possibly include some form of argument that gives
     *  the message list?
     */
    void onMeshMSMessage();
}
