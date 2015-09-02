package com.first.dash;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

// creates a receiver to make push notifications work
// taken from http://stackoverflow.com/questions/26154855/exception-when-opening-parse-push-notification
public class Receiver extends ParsePushBroadcastReceiver {

    @Override
    public void onPushOpen(Context context, Intent intent) {
    	// logs that the push has been clicked
    	Log.e("Push", "Clicked");
        
        // redirects to splash
        Intent i = new Intent(context, Splash.class);
        i.putExtras(intent.getExtras());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
