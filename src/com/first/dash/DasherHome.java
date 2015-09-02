package com.first.dash;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.GeoPoint;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

@SuppressLint("NewApi")

  /*

  * Adpapted from https://parse.com/tutorials/anywall

  */

  public class DasherHome extends BaseActivity implements LocationListener,

      GooglePlayServicesClient.ConnectionCallbacks,

      GooglePlayServicesClient.OnConnectionFailedListener {

    // request code to send Google
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;



    // establishes conversion constants

    // Milliseconds per second

    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final int FAST_CEILING_IN_SECONDS = 1;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND

        * UPDATE_INTERVAL_IN_SECONDS;
    
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND

        * FAST_CEILING_IN_SECONDS;
    
    private static final float METERS_PER_FEET = 0.3048f;
    private static final int METERS_PER_KILOMETER = 1000;
    private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;
    private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;
    private static final int MAX_POST_SEARCH_RESULTS = 20;
    private static final int MAX_POST_SEARCH_DISTANCE = 100;

    // establishes global variables

    private SupportMapFragment mapFragment;
    private float radius;
    private float lastRadius;

    // variables used in map updates
    private final Map<String, Marker> mapMarkers = new HashMap<String, Marker>();
    private int mostRecentMapUpdate;
    private boolean hasSetUpInitialLocation;
    private String selectedPostObjectId;
    private Location lastLocation;
    private Location currentLocation;



    // Location services request to be sent to google
    private LocationRequest locationRequest;

    // current value of LocationClient
    private LocationClient locationClient;

    // Adapter for the Parse query
    private ParseQueryAdapter<RequestPost> postsQueryAdapter;

    // where location is stored
    Geocoder geocoder;
    GeoPoint p;

	// establishes global variables for timer
	TextView textViewTime;
	String timeString, objectId;
	int seconds;
	long deadline, currentTime;
	ParseGeoPoint targetLocation;
	
	Button contactButton;
	String usernameString;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

      super.onCreate(savedInstanceState);
      setContentView(R.layout.dasher_home);
      super.onCreateDrawer();
		
      try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// finds the timer in the layout file
		textViewTime = (TextView) findViewById(R.id.textViewTime);		
		String username = ParseUser.getCurrentUser().getUsername();
		
		// searches for current dashes with the user
		ParseQuery<RequestPost> query = RequestPost.getQuery();
		query.whereEqualTo("Dasher", username);
		query.whereEqualTo("status", "Pending");
		query.getFirstInBackground(new GetCallback<RequestPost>() {
		  public void done(RequestPost object, ParseException e) {
		    if (e == null) {	
		    	// on success
		    	objectsWereRetrievedSuccessfully(object);
		    } else {
		      // on failure
		      textViewTime.setText("No dash to display");
		    }
		  }
		});	
		
		
	    // on-click handler for the submit button to send push notification
	    contactButton = (Button) findViewById(R.id.contact_submit);
	    contactButton.setOnClickListener(new OnClickListener() {
	      public void onClick(View v) {
	    	  
	    	  if (usernameString.equals("")){
	    		  Toast.makeText(DasherHome.this, "You can only send requests during a dash", Toast.LENGTH_LONG).show();
	    	  }
	    	  else{
	    		  DashMessage update = new DashMessage();
		    	  update.setMessage(((EditText) findViewById(R.id.contact_requester)).getText().toString());
		    	  update.setAsker(usernameString);
		    	  update.setInstallationId(ParseInstallation.getCurrentInstallation().getInstallationId());
		    	  
		    	  ParseACL acl = new ParseACL();
		    	  acl.setPublicReadAccess(true);
		    	  
		    	  update.setACL(acl);
		    	  update.saveInBackground();
		    	  
		    	  Toast.makeText(DasherHome.this, "Message Sent!", Toast.LENGTH_LONG).show();
			    
	    	  }
	        
	      }
	    });
		

      // gets the search radius
      radius = Dash.getSearchDistance();
      lastRadius = radius;

      // establishes a google location request
      locationRequest = LocationRequest.create();

      // sets time between updates for the locationRequest
      locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

      // uses high accuracy data
      locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);



      // Set the max length interval to one minute
      locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);



      // Creates the location client
      locationClient = new LocationClient(this, this, this);

      // instantiates the map
      mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.asker_map);

      // puts in the current location
      mapFragment.getMap().setMyLocationEnabled(true);
      
    }

	
	
	// creates the timer
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressLint("NewApi")
	public class CounterClass extends CountDownTimer {

		// establishes CounterClass
		public CounterClass(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		// sets the timer format
		@SuppressLint("NewApi")
		@TargetApi(Build.VERSION_CODES.GINGERBREAD)
		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
			
			long millis = millisUntilFinished;
			String hms = String.format("%02d:%02d",
					TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
					TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
			System.out.println(hms);
			textViewTime.setText(hms);
		}

		// after the timer has finished
		@Override
		public void onFinish() {
			textViewTime.setText("YOU FAILED.");
			// finds the post
			ParseQuery<RequestPost> timeEnd = RequestPost.getQuery();
			timeEnd.whereEqualTo("objectId", objectId);
			timeEnd.getFirstInBackground(new GetCallback<RequestPost>() {
			  public void done(RequestPost object, ParseException e) {
			    if (e == null) {
			    	object.put("status", "Complete");
			    	object.saveInBackground();
			    	
			    } else {
			      Log.d("RequestMoreInfo:", "Failed to end the time.");
			    }
			  }
			});
		}
		
		
		
	}  
	
  private void objectsWereRetrievedSuccessfully(RequestPost post){
	  
	  
		// finds the fields to be updated
		TextView username = (TextView) findViewById(R.id.dasher_home_username);             
		TextView payment = (TextView) findViewById(R.id.dasher_home_payment);        
		TextView item = (TextView) findViewById(R.id.dasher_home_item);        
		TextView itemCost = (TextView) findViewById(R.id.dasher_home_cost);   
		TextView time = (TextView) findViewById(R.id.dasher_home_time);
		
		EditText contact_requester = (EditText) findViewById(R.id.contact_requester);
		
		// updates the fields
		if (post.getUsername().toString() != null){
			username.setText("Username: " + post.getUsername().toString());
			usernameString = post.getUsername().toString();
		}
		if (post.getItem().toString() != null){
			item.setText("Item: " + post.getItem().toString());
		}
		if (post.getPayment().toString() != null){
			payment.setText("Payment: $" + post.getPayment().toString());
		}
		if (post.getItemCost().toString() != null){
			itemCost.setText("Item cost: $" + post.getItemCost().toString());
		}
		if (post.getTime().toString() != null){
			time.setText("Time: " + post.getTime().toString() + " minutes");
		}
		deadline = post.getDate();
		targetLocation = post.getTargetLocation();

		
		// gets the current time value
		currentTime = System.currentTimeMillis() / 1000L;

		//finds the difference between current and the deadline
		seconds = (int) (deadline - currentTime);
		
		// converts to the number of seconds left and minutes left
		int mins = (int) seconds / 60;
		int displaySec = seconds % 60;
		
		// converts time to strings
		String minsString = Integer.toString(mins);
		String displaySecString = Integer.toString(displaySec);
		
		// sets the timeString format
		if (Integer.parseInt(minsString) < 10)
		{
			minsString = "0" + minsString;
		}		
		if (Integer.parseInt(displaySecString) < 10)
		{
			displaySecString = "0" + displaySecString;
		}				
		timeString = minsString + ":" + displaySecString;				
		textViewTime.setText(timeString);		
		final CounterClass timer = new CounterClass(seconds*1000, 1000);	
		timer.start();
		

      MarkerOptions markerOpts =
    	        new MarkerOptions().position(new LatLng(targetLocation.getLatitude(), targetLocation.getLongitude()));
    	     

    	    // Displays a green marker with the information from the post
    	        markerOpts =
    	        markerOpts.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

    	        mapFragment.getMap().addMarker(markerOpts);		
	  }
    
    @Override
    // when the activity is stopped
    public void onStop() {
      // stops the updates
      if (locationClient.isConnected()) {
        stopPeriodicUpdates();

      }
      // disconnects the clients
      locationClient.disconnect();
      super.onStop();

    }

    // when the activity is restarted
    @Override

    public void onStart() {
      super.onStart();
      // connects to the location client
      locationClient.connect();

    }

    // on resume of the activity
    @Override

    protected void onResume() {
      super.onResume();
      // Get the latest search distance preference
      radius = Dash.getSearchDistance();

      // Goes to last known location if that is available

      if (lastLocation != null) {

        LatLng myLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

        // Updates the zoom dependent on a change in the search distance

        if (lastRadius != radius) {

          updateZoom(myLatLng);

        }

      }

    }



    // Logs dependent on network connection with google play services
    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

      // Switch statement depending on the code
      switch (requestCode) {



        // If there is a fail

        case CONNECTION_FAILURE_RESOLUTION_REQUEST:



          switch (resultCode) {

            // If the problem is resolved

            case Activity.RESULT_OK:
              if (Dash.APPDEBUG) {

                // log the successful result
                Log.d(Dash.APPTAG, "Connected to Google Play services");

              }



              break;

            // if there is some failure
            default:
              if (Dash.APPDEBUG) {
                // log the failure
                Log.d(Dash.APPTAG, "Could not connect to Google Play services");

              }
              break;
          }



          // unknown result
        default:

          if (Dash.APPDEBUG) {

            // log the unknown result
            Log.d(Dash.APPTAG, "Unknown request code received for the activity");
          }
          break;
      }

    }


    // function to check if google play services is connected
    private boolean servicesConnected() {

      // Check that Google Play services is available

      int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

      // there is a connection
      if (ConnectionResult.SUCCESS == resultCode) {

        if (Dash.APPDEBUG) {
          // log the connection
          Log.d(Dash.APPTAG, "Google play services available");

        }

        // Continue
        return true;

        // Google Play services was not available for some reason

      } 
      else {

        // set and display the error dialog
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
        if (dialog != null) {
          ErrorDialogFragment errorFragment = new ErrorDialogFragment();
          errorFragment.setDialog(dialog);
          errorFragment.show(getSupportFragmentManager(), Dash.APPTAG);

        }

        return false;

      }

    }

    // allows Dash to get location updates
    public void onConnected(Bundle bundle) {

      if (Dash.APPDEBUG) {

        Log.d("Connected to location services", Dash.APPTAG);

      }

      currentLocation = getLocation();

      startPeriodicUpdates();

    }

    // if disconnected from location services
    public void onDisconnected() {

      if (Dash.APPDEBUG) {
        Log.d("Disconnected from location services", Dash.APPTAG);
      }

    }

    // if there is no connection
    public void onConnectionFailed(ConnectionResult connectionResult) {

      // Try to use google functionality to resolve the error
      if (connectionResult.hasResolution()) {

        try {
          // Start activity to solve ther error
          connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

        } 
        catch (IntentSender.SendIntentException e) {
          if (Dash.APPDEBUG) {

            // if there is a failure with the intent
            Log.d(Dash.APPTAG, "An error occurred when connecting to location services.", e);

          }

        }

      } else {

        // Display the error to the user
        showErrorDialog(connectionResult.getErrorCode());

      }

    }


    // Report location changes to the UI if they are greater than 10 meters
    public void onLocationChanged(Location location) {

      currentLocation = location;

      if (lastLocation != null
          && geoPointFromLocation(location)
          .distanceInKilometersTo(geoPointFromLocation(lastLocation)) < 0.01) {
        return;

      }

      // feeds the location to lastLocation
      lastLocation = location;
      
      // gets latitude and longitude coordinates of the location
      LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
      
      if (!hasSetUpInitialLocation) {

        // Zoom to current location on the map

        updateZoom(myLatLng);

        hasSetUpInitialLocation = true;

      }

    }


    // sends a user's location to location services
    private void startPeriodicUpdates() {
      locationClient.requestLocationUpdates(locationRequest, this);

    }

    // remove location updates on request
    private void stopPeriodicUpdates() {

      locationClient.removeLocationUpdates(this);

    }

    // gets location
    private Location getLocation() {

      // If Google Play Services is available

      if (servicesConnected()) {

        // Get the current location

        return locationClient.getLastLocation();

      } else {

        return null;

      }

    }


    // converts location to a ParseGeoPoint
    private ParseGeoPoint geoPointFromLocation(Location loc) {

      return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());

    }

    // handles zooming in the map when there is a new search radius
    private void updateZoom(LatLng myLatLng) {

      // Gets the bounds
      LatLngBounds bounds = calculateBoundsWithCenter(myLatLng);

      // Zoom to fit within the given bounds
      mapFragment.getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));

    }

    // calculates the map bounds
    private double calculateLatLngOffset(LatLng myLatLng, boolean bLatOffset) {

      // Default return
      double latLngOffset = OFFSET_CALCULATION_INIT_DIFF;

      // calculates the desired distance based on radius
      float desiredOffsetInMeters = radius * METERS_PER_FEET;

      // variables for calculation
      float[] distance = new float[1];
      boolean foundMax = false;
      double foundMinDiff = 0;

      // looks for correct offset
      do {

        // distance between point and current map bounds

        if (bLatOffset) {

          Location.distanceBetween(myLatLng.latitude, myLatLng.longitude, myLatLng.latitude

              + latLngOffset, myLatLng.longitude, distance);
        } 

        // if a distance update is necessary
        else {
          Location.distanceBetween(myLatLng.latitude, myLatLng.longitude, myLatLng.latitude,

              myLatLng.longitude + latLngOffset, distance);
        }

        // compares current location with the new one

        float distanceDiff = distance[0] - desiredOffsetInMeters;
        if (distanceDiff < 0) {

          // if the map needs to get larger
          if (!foundMax) {

            foundMinDiff = latLngOffset;

            // Increase the calculated offset

            latLngOffset *= 2;

          } else {

            double tmp = latLngOffset;

            // Increase the calculated offset, at a slower pace

            latLngOffset += (latLngOffset - foundMinDiff) / 2;

            foundMinDiff = tmp;

          }

        } else {

          // Overshot the desired distance

          // Decrease the calculated offset

          latLngOffset -= (latLngOffset - foundMinDiff) / 2;

          foundMax = true;

        }

      } while (Math.abs(distance[0] - desiredOffsetInMeters) > OFFSET_CALCULATION_ACCURACY);

      return latLngOffset;

    }

    // calculates map bounds
    LatLngBounds calculateBoundsWithCenter(LatLng myLatLng) {

      // new variable to store the bounds
      LatLngBounds.Builder builder = LatLngBounds.builder();



      // calculate the east and west maximum that can be shown

      double lngDifference = calculateLatLngOffset(myLatLng, false);

      LatLng east = new LatLng(myLatLng.latitude, myLatLng.longitude + lngDifference);

      builder.include(east);

      LatLng west = new LatLng(myLatLng.latitude, myLatLng.longitude - lngDifference);

      builder.include(west);



      // Calculate the north and south maximum that can be shown

      double latDifference = calculateLatLngOffset(myLatLng, true);

      LatLng north = new LatLng(myLatLng.latitude + latDifference, myLatLng.longitude);

      builder.include(north);

      LatLng south = new LatLng(myLatLng.latitude - latDifference, myLatLng.longitude);

      builder.include(south);



      return builder.build();

    }

    // shows the error returned by google
    private void showErrorDialog(int errorCode) {

      // retreives error from google

      Dialog errorDialog =

          GooglePlayServicesUtil.getErrorDialog(errorCode, this,

              CONNECTION_FAILURE_RESOLUTION_REQUEST);

      // if there is an error to display
      if (errorDialog != null) {

        // creates the dialog to show the error
        ErrorDialogFragment errorFragment = new ErrorDialogFragment();

        // sets the dialog in the fragment
        errorFragment.setDialog(errorDialog);

        // shows the error
        errorFragment.show(getSupportFragmentManager(), Dash.APPTAG);

      }

    }

  // getting the GeoPoint of the inputed address using Geocoder
    
    private void searchAddress(String address) {
  	  
  	  // creates the new geocoder
  	  geocoder = new Geocoder(this);
  	  
      // tries to get the addresses from geocoder
      try {
  		List<Address> addresses = geocoder.getFromLocationName(address, 5);
  		
  		if(addresses != null && addresses.size() > 0)
  		{
  			// sets the GeoPoint p with the first address found
        p = new GeoPoint((int) (addresses.get(0).getLatitude() * 1E6), (int) (addresses.get(0).getLongitude() * 1E6));
  		}
  		
  	} catch (IOException e) {
  		e.printStackTrace();
  	}          
  	  
    }

    // fragment shows the error
    public static class ErrorDialogFragment extends DialogFragment {

      // Global field to contain the error dialog

      private Dialog mDialog;


      // sets the default dialog field to null
      public ErrorDialogFragment() {

        super();

        mDialog = null;

      }


      // sets the dialog to display
      public void setDialog(Dialog dialog) {

        mDialog = dialog;

      }


      // always returns a dialog to the method even if it is null
      @Override

      public Dialog onCreateDialog(Bundle savedInstanceState) {

        return mDialog;

      }

    }

  }
