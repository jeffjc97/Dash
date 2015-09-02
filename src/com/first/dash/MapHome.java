package com.first.dash;



import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import com.first.dash.RequestMoreInfo.DrawerItemClickListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.GeoPoint;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
//import android.location.LocationListener;
//import android.location.LocationManager;

/*

* Adpapted from https://parse.com/tutorials/anywall

*/

public class MapHome extends BaseActivity implements LocationListener,

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
  
  //string to store current entry in search
  private String search;

  @Override

  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_map);
    super.onCreateDrawer();
    
//    if (ParseUser.getCurrentUser() == null)
//    {
//    	Intent intent = new Intent(MapHome.this, Login.class);
//        startActivity(intent);
//    }
    
    // gets the search radius
    radius = Dash.getSearchDistance();
    lastRadius = radius;
    //setContentView(R.layout.main_map);

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

    EditText myTextBox = (EditText) findViewById(R.id.search);
    myTextBox.addTextChangedListener(new TextWatcher() {
   
     public void afterTextChanged(Editable s) {
     }
   
     public void beforeTextChanged(CharSequence s, int start, 
       int count, int after) {
     }
   
     public void onTextChanged(CharSequence s, int start, 
     int before, int count) {
    	 
    	 final StringBuilder sb = new StringBuilder(s.length());
	     sb.append(s);
	     search = sb.toString();
    	 doListQuery();
    	 
     }
    });
    
    
    
    // Set up ParseQuery searching for posts in the area
    ParseQueryAdapter.QueryFactory<RequestPost> factory =

        new ParseQueryAdapter.QueryFactory<RequestPost>() {

          public ParseQuery<RequestPost> create() {

            Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;

            ParseQuery<RequestPost> query = RequestPost.getQuery();

            query.include("user");

            query.orderByDescending("createdAt");

            query.whereWithinKilometers("targetLocation", geoPointFromLocation(myLoc), radius

               * METERS_PER_FEET / METERS_PER_KILOMETER);
            
            query.whereEqualTo("status", "Unanswered");
            
            query.whereGreaterThan("deleteTime", System.currentTimeMillis() / 1000L);
            
            query.setLimit(MAX_POST_SEARCH_RESULTS);
            
            if (search != null)
            {
            	query.whereContains("item", search);
            }
            

            return query;

          }

        };



    // Used to establish the list of posts
    postsQueryAdapter = new ParseQueryAdapter<RequestPost>(this, factory) {

      @Override

      public View getItemView(RequestPost post, View view, ViewGroup parent) {

        // creates a new list item
        if (view == null) {

          view = View.inflate(getContext(), R.layout.main_map_item, null);

        }
        
        // finds the fields to be updated
        TextView paymentView = (TextView) view.findViewById(R.id.payment_view);        
        TextView itemView = (TextView) view.findViewById(R.id.item_view);        
        TextView timeView = (TextView) view.findViewById(R.id.time_view);
        
        // updates the fields
        paymentView.setText("$"+post.get("payment").toString());
        itemView.setText(post.get("item").toString());
        timeView.setText(post.get("time").toString());

        return view;

      }

    };



    // Disable automatic loading when the adapter is attached to a view.
    postsQueryAdapter.setAutoload(false);

    // prevents scrolling as only a certain number of queries are allowed
    postsQueryAdapter.setPaginationEnabled(false);



    // Attach the query adapter to the view
    ListView postsListView = (ListView) findViewById(R.id.posts_listview);
    postsListView.setAdapter(postsQueryAdapter);
    onResume();

    // OnClick handler for when a menu item is clicked
    postsListView.setOnItemClickListener(new OnItemClickListener() {

      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        final RequestPost item = postsQueryAdapter.getItem(position);
        Intent intent = new Intent(MapHome.this, RequestMoreInfo.class);
        intent.putExtra("id",item.getObjectId());
        startActivity(intent);
      }

    });

    // instantiates the map
    mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.asker_map);

    // puts in the current location
    mapFragment.getMap().setMyLocationEnabled(true);

    // updates map on change of the camera
    mapFragment.getMap().setOnCameraChangeListener(new OnCameraChangeListener() {
      public void onCameraChange(CameraPosition position) {
        doMapQuery();
      }
    });
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

    // Updates the map and the list

    doMapQuery();

    doListQuery();

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

    // Update map radius indicator

    doMapQuery();

    // update the list
    doListQuery();

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

  // refreshes listview if there is new location data
  private void doListQuery() {

    Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;

    // load info only if there is a stored location
    if (myLoc != null) {

      // sets the new data
      postsQueryAdapter.loadObjects();

    }

  }

  // updates the map view
  private void doMapQuery() {

    final int myUpdateNumber = ++mostRecentMapUpdate;

    Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;

    // gets rid of existing markers if location isn't available

    if (myLoc == null) {

      cleanUpMarkers(new HashSet<String>());

      return;

    }

    final ParseGeoPoint myPoint = geoPointFromLocation(myLoc);

    // queries data from parse

    ParseQuery<RequestPost> mapQuery = RequestPost.getQuery();

    // specifies requirement
    mapQuery.whereWithinKilometers("targetLocation", myPoint, MAX_POST_SEARCH_DISTANCE);
    mapQuery.whereEqualTo("status", "Unanswered");
    mapQuery.whereGreaterThan("deleteTime", System.currentTimeMillis() / 1000L);
    mapQuery.include("user");
    mapQuery.orderByDescending("createdAt");
    mapQuery.setLimit(MAX_POST_SEARCH_RESULTS);

    // Finds the query in the background
    mapQuery.findInBackground(new FindCallback<RequestPost>() {
      @Override

      public void done(List<RequestPost> objects, ParseException e) {

        // if there is an error finding the data
        if (e != null) {

          if (Dash.APPDEBUG) {

            // logs the error
            Log.d(Dash.APPTAG, "An error occurred while querying for map posts.", e);
          }

          return;

        }

        // gets results only from the most recent update
        if (myUpdateNumber != mostRecentMapUpdate) {

          return;

        }

        // for the posts that will be shown on the map
        Set<String> toKeep = new HashSet<String>();

        // Loops through declaring each RequestPost as post
        for (RequestPost post : objects) {

          // adds the marker to toKeep
          toKeep.add(post.getObjectId());

          // checks to see if the marker already exists
          Marker oldMarker = mapMarkers.get(post.getObjectId());

          // Set up the map marker's location
          searchAddress(post.get("address1").toString());
          
          MarkerOptions markerOpts =

              new MarkerOptions().position(new LatLng(p.getLatitudeE6()/1000000., p.getLongitudeE6()/1000000.));


            // Displays a green marker with the information from the post
            markerOpts =
                markerOpts.title(post.getItem()).snippet("$" + post.getPayment())                

                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));


          // Puts a new marker on the map
          Marker marker = mapFragment.getMap().addMarker(markerOpts);

          // places the current marker in mapMarker
          mapMarkers.put(post.getObjectId(), marker);

          // sets the marker to show the info window when selected
          if (post.getObjectId().equals(selectedPostObjectId)) {

            marker.showInfoWindow();

            selectedPostObjectId = null;

          }

        }

        // Gets rid of all the old markers
        cleanUpMarkers(toKeep);

      }

    });

  }

  // functionality to clean up the old markers
  private void cleanUpMarkers(Set<String> markersToKeep) {

    for (String objId : new HashSet<String>(mapMarkers.keySet())) {

      // if the marker isn't in markersToKeep
      if (!markersToKeep.contains(objId)) {

        Marker marker = mapMarkers.get(objId);

        // delete the marker
        marker.remove();
        mapMarkers.get(objId).remove();
        mapMarkers.remove(objId);

      }

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