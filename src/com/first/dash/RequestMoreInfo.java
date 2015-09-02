package com.first.dash;


import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Activity which displays a login screen to the user, offering registration as well.
 */
public class RequestMoreInfo extends Activity {
    
    // establishes global variables
	String objectId;
	String timeReq;
	String asker;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
    setContentView(R.layout.request_more_info);       
    
    // gets the extra id of the post passed from MapHome
	Intent intent = getIntent();
	objectId = intent.getStringExtra(Dash.INTENT_EXTRA_ID);
    
    // queries for the post
	ParseQuery<RequestPost> query = RequestPost.getQuery();
	query.whereEqualTo("objectId", objectId);
	query.getFirstInBackground(new GetCallback<RequestPost>() {
	  public void done(RequestPost object, ParseException e) {
	    // on successful query
	    if (e == null) {
	    	objectsWereRetrievedSuccessfully(object);
	    } 
	    // on query failure
	    else {
	      Log.d("RequestMoreInfo:", "Failed to retrieve the object.");
	    }
	  }
	});
	
	// finds dashButton in the layout file
	Button dashButton = (Button) findViewById(R.id.request_more_info_dash);
    
	// onClick handler for dash
    dashButton.setOnClickListener(new OnClickListener(){
    	public void onClick(View v){
    		
    		setDash();   		
            Intent intent = new Intent(RequestMoreInfo.this, MapHome.class);
            
            // creates a new dash post
            String deviceToken = ParseInstallation.getCurrentInstallation().getInstallationId();
            DashPost dashPost = new DashPost();
    		dashPost.setDasher(ParseUser.getCurrentUser().getUsername());
    		dashPost.setAsker(asker);
    		dashPost.setBoolean(true);
    		dashPost.setInstallationId(deviceToken);
            
    		ParseACL acl = new ParseACL();

    	    // Give public read access
    	    acl.setPublicReadAccess(true);
    	    dashPost.setACL(acl);
    		
    		dashPost.saveInBackground();
            
            intent.putExtra("id", objectId);    		
    		startActivity(intent);
    	}
    });    
    
  }
	
  private void objectsWereRetrievedSuccessfully(RequestPost post){
    
	// finds the fields to be updated
	TextView username = (TextView) findViewById(R.id.request_more_info_username);             
	TextView payment = (TextView) findViewById(R.id.request_more_info_payment);        
	TextView item = (TextView) findViewById(R.id.request_more_info_item);        
	TextView itemCost = (TextView) findViewById(R.id.request_more_info_item_cost);   
	TextView time = (TextView) findViewById(R.id.request_more_info_time);
	TextView totalAddress = (TextView) findViewById(R.id.request_more_info_total_address);
	TextView addressNotes = (TextView) findViewById(R.id.request_more_info_address_notes);
	TextView extraInfo = (TextView) findViewById(R.id.request_more_info_extra_info);

	
	// updates the fields
	if (post.getUsername().toString() != null){
		asker = post.getUsername().toString();
		username.setText("Username: " + asker);		
	}	
	if (post.getItem().toString() != null){
		item.setText("Item: " + post.getItem().toString());
	}
	if (post.getPayment().toString() != null){
		payment.setText("Payment " + post.getPayment().toString());
	}
	if (post.getItemCost().toString() != null){
		itemCost.setText("Item cost: " + post.getItemCost().toString());
	}
	if (post.getTime().toString() != null){
		time.setText("Time: " + post.getTime().toString());
		timeReq = post.getTime().toString();
	}
	if (post.getExtraInfo().toString() != null){
		extraInfo.setText("Extra Information: " + post.getExtraInfo().toString());
	}	
	if (post.getAddressOne().toString() != null){
		totalAddress.setText("Address: " + post.getAddressOne().toString());
	}	
//	if (post.getAddressTwo().toString() != null){
//		addressNotes.setText("Address notes: " + post.getAddressTwo().toString());
//	}	


  }
  
  public void setDash()
  {
  		// gets the time at which the request will be fulfilled
		final long fulfillTime = (System.currentTimeMillis() / 1000L) + Integer.parseInt(timeReq)*60;
	  	ParseQuery<RequestPost> queryTime = RequestPost.getQuery();
		queryTime.whereEqualTo("objectId", objectId);
		queryTime.getFirstInBackground(new GetCallback<RequestPost>() {
		  public void done(RequestPost object, ParseException e) {
		    // on successful query
		    if (e == null) {
		    	// updates the request
		    	object.put("fulfillTime", fulfillTime);
		    	object.put("status", "Pending");
		    	object.put("Dash", true);
		    	object.put("Dasher", ParseUser.getCurrentUser().getUsername());
		    	// saves the object in parse
		    	object.saveInBackground();
		    	
		    } 
		    // on query failure
		    else {
		      Log.d("RequestMoreInfo:", "Failed to set the time.");
		    }
		  }
		});	  
  }
  
}