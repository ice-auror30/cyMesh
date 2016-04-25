

/**
 * Created by Adit
 */
package org.servalproject.sensors;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class intentListener extends BroadcastReceiver
{
    // Get the request to fire off the intent

    public void onReceive(Context context, Intent intent) {

        //Todo based on team discussion
        //Process the intent using the string supplied in the intent
        String request = "";
        try {

            intentService.getInstance().processRequest(request);

        }
        catch (Exception e) {
          e.printStackTrace();
        }
    }
}
