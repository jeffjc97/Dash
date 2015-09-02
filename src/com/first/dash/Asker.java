package com.first.dash;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Activity which allows users to make requests
 */
public class Asker extends BaseActivity {

  // creates references to the items the user has filled in
  private EditText item, payment, time, item_cost, extra_info, address1, address2;
  
  // establishes global variables 
  private String itemString, paymentString, timeString, item_costString, extra_infoString, address1String, address2String,formattedPaymentString, formattedItemCost;
  private Button postButton;

  // spinner for the time select
  private Spinner spinner;
  // geopoint for location data
  private ParseGeoPoint geoPoint;
  // where location is stored
  private Geocoder geocoder;
  private ParseGeoPoint q;
  private String estimateLoc = "";
  
  boolean searchResult, postResult;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.request);
    super.onCreateDrawer();

    // when the time field is set
    addListenerOnSpinnerItemSelection();

    // gets the location passed from MapHome
    //Intent intent = getIntent(); 
    //final Location location = intent.getParcelableExtra(Dash.INTENT_EXTRA_LOCATION);
    
    //geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
    
    // on-click handler for the submit button
    postButton = (Button) findViewById(R.id.asker_submit);
    postButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        getLocation();
        searchAddress(address1String);
		if (searchResult)
        {	
    	  post();
          // redirect to the main map
          if (postResult){
	    	  Intent intent = new Intent(Asker.this, MapHome.class);
	          startActivity(intent);
          }
        }
        else
        {	
        	Toast.makeText(Asker.this, "Location not found, try again", Toast.LENGTH_LONG).show();
        }

      }
    });
      
    // creates the time spinner
    Spinner spinner = (Spinner) findViewById(R.id.minutes_spinner);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.minutes_array, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter); 
    
    EditText myTextBox = (EditText) findViewById(R.id.address_1);
    myTextBox.addTextChangedListener(new TextWatcher() {
   
     public void afterTextChanged(Editable s) {
     }
   
     public void beforeTextChanged(CharSequence s, int start, 
       int count, int after) {
     }
   
     public void onTextChanged(CharSequence s, int start, 
     int before, int count) {
	     TextView myOutputBox = (TextView) findViewById(R.id.estimate_address);
	     
	     if (s.length() > 3)
	     {
		     final StringBuilder sb = new StringBuilder(s.length());
		     sb.append(s);
		     estimateAddress(sb.toString());
		     myOutputBox.setText(estimateLoc);
	     }
	     else
	     {
	    	 myOutputBox.setText("Start entering your address!");
	     }
     }
    });
    
    
  }  
    
    
  
 
  
  public void addListenerOnSpinnerItemSelection() {
	      spinner = (Spinner) findViewById(R.id.minutes_spinner);
	      spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
    	    @Override
    	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
    	    	
            // gets the selection of time from the user
            timeString = parentView.getItemAtPosition(position).toString();
    	    }

    	    @Override
    	    public void onNothingSelected(AdapterView<?> parentView) {
    	    }
	    });    
  }

  private void searchAddress(String address) {
	  
	  // creates the new geocoder
	  geocoder = new Geocoder(this);
	  
    // tries to get the addresses from geocoder
    try {
		List<Address> addresses = geocoder.getFromLocationName(address, 5);
		if(addresses != null && addresses.size() > 0)
		{
			// sets the GeoPoint q with the first address found	
			q = new ParseGeoPoint((addresses.get(0).getLatitude()), (addresses.get(0).getLongitude()));
			searchResult = true;
		}

		
	} catch (IOException e) {
		e.printStackTrace();
	}          
	  
  }
  
  private void estimateAddress(String address) {
	  
	  // creates the new geocoder
	  geocoder = new Geocoder(this);
	  
    // tries to get the addresses from geocoder
    try {
		List<Address> addresses = geocoder.getFromLocationName(address, 5);
		if(addresses != null && addresses.size() > 0)
		{
					
			String geoAddress = addresses.get(0).getAddressLine(0);
			String city = addresses.get(0).getAddressLine(1);
			String country = addresses.get(0).getAddressLine(2);			
			estimateLoc = ((geoAddress == null) ? "" : geoAddress) + ((city == null) ? "" : ", " + city) + ((country == null) ? "" : ", " + country);
		}

		
	} catch (IOException e) {
		e.printStackTrace();
	}          
	  
  }
  
  
  
  private void getLocation() {
	// finds the values in the post for address
	address1 = (EditText) findViewById(R.id.address_1);
	//address2 = (EditText) findViewById(R.id.address_2); 
	
	// converts inputed text to strings;
	address1String = address1.getText().toString().trim();
	//address2String = address2.getText().toString().trim();
  }
  
  private void post () {
    
	// finds the values in the post
	item = (EditText) findViewById(R.id.request_item);
	payment = (EditText) findViewById(R.id.request_payment);
	item_cost = (EditText) findViewById(R.id.request_cost);
	extra_info = (EditText) findViewById(R.id.extra_info);
	

	
	// converts inputed text to strings
	itemString = item.getText().toString().trim();
	
	paymentString = payment.getText().toString().trim();

	item_costString = item_cost.getText().toString().trim();

	extra_infoString = extra_info.getText().toString().trim();

	if (itemString.equals("") || timeString.equals("Time (minutes):") || paymentString.equals("") || item_costString.equals("") || extra_infoString.equals("")){
		Toast.makeText(Asker.this, "Fill out all of the required fields!", Toast.LENGTH_LONG).show();
	}
	
	else{
		postResult = true;	
		float floatPayment = Float.parseFloat(paymentString);
		formattedPaymentString = String.format(Locale.US, "%.2f", floatPayment);
		float floatCost = Float.parseFloat(item_costString);
		formattedItemCost = String.format(Locale.US, "%.2f", floatCost);
		long curTime = System.currentTimeMillis() / 1000L;
		int timeSecs = Integer.parseInt(timeString) * 60;
		
		
	    // Set up a progress dialog
	    final ProgressDialog dialog = new ProgressDialog(Asker.this);
	    dialog.setMessage("Loading...");
	    dialog.show();
	
	    // Create a post.
	    RequestPost post = new RequestPost();
	
	    // Sets the values of the data to be sent to Parse
	    // post.setLocation(geoPoint);
	    post.setItem(itemString);
	    post.setPayment(formattedPaymentString);
	    post.setTime(timeString);
	    post.setItemCost(formattedItemCost);
	    post.setExtraInfo(extra_infoString);
	    post.setAddressOne(address1String);
	    //post.setAddressTwo(address2String);
	    post.setStatus("Unanswered");
	    post.setPostTime(curTime);
	    post.setDeleteTime(curTime + 5 * timeSecs);
	    post.setUser(ParseUser.getCurrentUser());
	    post.setUsername(ParseUser.getCurrentUser().getUsername());
	    post.setTargetLocation(q);
	    
	    
	    // setting permissions with ParseACL (chmod for parse)
	    ParseACL acl = new ParseACL();
	    // Give public read and write access
	    acl.setPublicReadAccess(true);
	    acl.setPublicWriteAccess(true);
	    post.setACL(acl);
	
	    // Save the post in the background
	    post.saveInBackground(new SaveCallback() {
	      @Override
	      public void done(ParseException e) {
	        dialog.dismiss();
	        finish();
	      }
	    });
	  }
  }
}