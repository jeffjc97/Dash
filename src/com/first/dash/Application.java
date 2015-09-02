package com.first.dash;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.PushService;


public class Application extends android.app.Application{
	@Override
	public void onCreate() {
		super.onCreate();
		
		// initializes the parse account and registers parseobject extensions

		Parse.initialize(this, "ubw8fExE7DAZgsmigdZbITs2RMKhPeCSu9A2HfDh", "fJ0ahXzOg16I5by9WgEluvnRS6kBz8mLcF6ULm3a");

		PushService.setDefaultPushCallback(this, Splash.class);

		ParseInstallation.getCurrentInstallation().saveInBackground();

		ParseObject.registerSubclass(RequestPost.class);

		ParseObject.registerSubclass(DashPost.class);
		
		ParseObject.registerSubclass(DashMessage.class);
	}

}