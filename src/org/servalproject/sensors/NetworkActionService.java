//package org.servalproject.sensors;
//
//import android.app.Service;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Message;
//import android.os.Messenger;
//import android.os.RemoteException;
//import android.widget.Toast;
//
//public class NetworkActionService extends Service {
//    /** Command to the service to display a message */
//    static final int MSG_SAY_HELLO = 1;
//    static final int CLIENTMESSENGER = 2;
//    static final String action = "edu.iastate.ejfett4.NetworkAction";
//    private Messenger client;
//
//    /**
//     * Handler of incoming messages from clients.
//     */
//    class IncomingHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case MSG_SAY_HELLO:
//                    //SurfaceView view = (SurfaceView) msg.obj;
//
//                    //MainActivity.rc.startStopRecording();
//                    Toast.makeText(getApplicationContext(), "hello!", Toast.LENGTH_SHORT).show();
//                    Message otherMsg = Message.obtain(null, MainActivity.MSG_SET_TEXT, 0, 0);
//                    Bundle b = new Bundle();
//                    b.putString("message", "HelloReturnText");
//                    otherMsg.obj = b;
//                    try
//                    {
//                        client.send(otherMsg);
//                    }
//                    catch(RemoteException e)
//                    {
//                        e.printStackTrace();
//                    }
//                    break;
//                case CLIENTMESSENGER:
//                    client = msg.replyTo;
//                    Message returnMsg = Message.obtain(null, MainActivity.MSG_SET_TEXT, 0, 0);
//
//                    Bundle bb = new Bundle();
//                    bb.putString("message", "NewText");
//                    returnMsg.obj = bb;
//                    try
//                    {
//                        client.send(returnMsg);
//                    }
//                    catch(RemoteException e)
//                    {
//                        e.printStackTrace();
//                    }
//                    break;
//                default:
//                    super.handleMessage(msg);
//            }
//        }
//    }
//
//    /**
//     * Target we publish for clients to send messages to IncomingHandler.
//     */
//    final Messenger mMessenger = new Messenger(new IncomingHandler());
//
//    /**
//     * When binding to the service, we return an interface to our messenger
//     * for sending messages to the service.
//     */
//    @Override
//    public IBinder onBind(Intent intent) {
//        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
//        return mMessenger.getBinder();
//    }
//}
