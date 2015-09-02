package com.first.dash;


public class Dash extends android.app.Application {
  // Debugging switch
  public static final boolean APPDEBUG = false;

  // Debugging tag for the application
  public static final String APPTAG = "AnyWall";

  // Used to pass location between activities
  public static final String INTENT_EXTRA_LOCATION = "location";
  
  // Used in passing the id between activities
  public static final String INTENT_EXTRA_ID = "id";

  // establishes default search distance
  private static final float DEFAULT_SEARCH_DISTANCE = 5280.0f;


  public Dash() {
  }

  @Override
  public void onCreate() {
    super.onCreate();

  }

  public static float getSearchDistance() {
    //returns default search distance
	  return DEFAULT_SEARCH_DISTANCE;
  }

}