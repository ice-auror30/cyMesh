package org.servalproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.servalproject.batphone.CallHandler;
import org.servalproject.messages.ShowConversationActivity;
import org.servalproject.servald.Peer;
import org.servalproject.servald.PeerListService;
import org.servalproject.servald.ServalD;
import org.servalproject.servaldna.ServalDCommand;
import org.servalproject.servaldna.SubscriberId;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by jasonwong on 3/16/16.
 */
public class VisualizationActivity extends Activity {
    /** Called when the activity is first created. */

    WebView wv;

    JavaScriptInterface JSInterface;

    HashSet<String> nodes = new HashSet<String>();
    HashSet<String> edges = new HashSet<String>();
    ConcurrentMap<SubscriberId, Peer> peers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visual);
        wv = (WebView)findViewById(R.id.webview);

        wv.getSettings().setJavaScriptEnabled(true);

        final String jsonStr = parseRoutingTable();

        JSInterface = new JavaScriptInterface(this);
        wv.addJavascriptInterface(JSInterface, "Android");

        wv.loadUrl("file:///android_asset/visual.html");

        wv.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                wv.loadUrl("javascript:initNetwork('" + jsonStr + "')");
            }
        });

    }

    public class JavaScriptInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public String requestSensor(String sid)
        {
            return "TEST1: " + sid;
        }

        @JavascriptInterface
        public String requestVideo(String sid)
        {
            return "TEST2: " + sid;
        }

        @JavascriptInterface
        public String requestAudio(String sid)
        {
            return "TEST3: " + sid;
        }

        @JavascriptInterface
        public void startChat(String sid)
        {
            ServalBatPhoneApplication app = ServalBatPhoneApplication.context;
            if (!ServalD.isRhizomeEnabled()) {
                app.displayToastMessage("Messaging cannot function without an sdcard");
                return;
            }

            // Send MeshMS by SID
            Intent intent = new Intent(
                    app, ShowConversationActivity.class);
            intent.putExtra("recipient", sid);
            mContext.startActivity(intent);
        }

        @JavascriptInterface
        public void startCall(String sid)
        {
            try {
                peers.get(sid);

                SubscriberId sidObject = new SubscriberId(sid);

                if (peers.get(sidObject) != null) {
                    CallHandler.dial(peers.get(sidObject));
                    Log.i("CALLING", "calling selected peer");
                }

            }catch(Exception e){
                Log.i("CALLINGFAIL",e.getMessage());
            }
        }

        @JavascriptInterface
        public String updateNetwork() {
            return parseRoutingTable();
        }

    }

    private String parseRoutingTable(){
        peers = PeerListService.peers;

        JSONArray jsonArrayNodes = new JSONArray();
        JSONArray jsonArrayEdges = new JSONArray();

        //read route table
        try {
            ServalDCommand.RouteTable rt = ServalDCommand.routeTable();
            List<String> sids = rt.sids;
            List<String> flags = rt.flags;
            List<String> interfaces = rt.interfaces;
            List<String> nexthops = rt.nexthops;
            List<String> priorhops = rt.priorhops;

            Log.i("ROW_SIZES", Integer.toString(sids.size()) + "," + Integer.toString(flags.size()) + "," + Integer.toString(interfaces.size()) + "," + Integer.toString(nexthops.size()) + "," + Integer.toString(priorhops.size()));
            for(int i = 0; i < sids.size(); i++){

                Log.i("ROW", sids.get(i) + "," + flags.get(i) + "," + interfaces.get(i) + "," + nexthops.get(i) + "," + priorhops.get(i));

                JSONObject node = new JSONObject();
                try {

                    node.put("id", sids.get(i));
                    String name = "SELF";
                    SubscriberId sid = new SubscriberId(sids.get(i));
                    if(peers.get(sid)!= null){
                        name = peers.get(sid).getDisplayName();
                    }
                    node.put("name", name);
                    node.put("phone", "555");
                    if(!"SELF".equals(flags.get(i))) {
                        node.put("color", "#000000");
                    }else{
                        node.put("color", "#0040ff");
                    }
                    nodes.add(sids.get(i));

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                jsonArrayNodes.put(node);

                if(!"SELF".equals(flags.get(i))){
                    JSONObject edge = new JSONObject();
                    try {
                        edge.put("source", sids.get(i));
                        edge.put("target", priorhops.get(i));

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    jsonArrayEdges.put(edge);

                    edges.add(sids.get(i) + " " + priorhops.get(i));
                }
            }

        }catch(Exception e){
            Log.e("ROUTE", e.getMessage());
        }

        //I/ROW     ( 2040): 852B967D1FF4CDC8C5033A62562B94A1291C213D4B9E8190C8A36A93C14CE807,BROADCAST,wlan0,
        // NA_NEIGH,C11D01A43D0F69A689FD1A93C9654AA48756E65C2760A006F2D04AD3B2BB7251
        //I/ROW     ( 2040): C11D01A43D0F69A689FD1A93C9654AA48756E65C2760A006F2D04AD3B2BB7251,SELF,,NA_NEIGH,NA_PRIOR

        JSONObject networkObj = new JSONObject();
        try{
            networkObj.put("nodes", jsonArrayNodes);
            networkObj.put("edges", jsonArrayEdges);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return networkObj.toString();
    }
}
