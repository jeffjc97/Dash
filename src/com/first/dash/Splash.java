package com.first.dash;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class Splash extends Activity{

	@Override
	protected void onCreate(Bundle IntroScreen) {
		
		super.onCreate(IntroScreen);
		
		ParsePush.subscribeInBackground("", new SaveCallback() {
			  @Override
			  public void done(ParseException e) {
			    if (e == null) {
			      Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
			    } else {
			      Log.e("com.parse.push", "failed to subscribe for push", e);
			    }
			  }
			});
		
		setContentView(R.layout.splash);
		Thread timer = new Thread(){
			public void run(){
				try{
					sleep(1000);
				}
				catch (InterruptedException e){
					e.printStackTrace();
				}
				finally{
					
					if (ParseUser.getCurrentUser() != null) {
						  Intent openMainMenu = new Intent("com.first.dash.MAPHOME");
						  startActivity(openMainMenu);
						} 

					else {
//							Globals g = Globals.getInstance();
//					    	g.setLogin(true);
							Intent openStartingPoint = new Intent("com.first.dash.MENU");
							startActivity(openStartingPoint);
						}

				}
			}
		};
		timer.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}

}
