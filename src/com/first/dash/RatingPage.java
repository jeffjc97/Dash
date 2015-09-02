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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Activity which allows users to rate their transaction
 */
public class RatingPage extends Activity {
 
  // establishes global variables 
  private Button postButton;
  private String ratingString;
  
  String dasherName;
  boolean postResult;
  
  // spinner for the time select
  private Spinner spinner;
  private RatingBar ratingBar;
  
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.rating_page);
    
    // gets the extra id of the post passed from AskerHome
    Intent intent = getIntent();
    dasherName = intent.getStringExtra("dasher");

    addListenerOnRatingBar();

    // on-click handler for the submit button
    postButton = (Button) findViewById(R.id.rating_submit);
    postButton.setOnClickListener(new OnClickListener(){
    	public void onClick(View v) {
    		post();
    		if (postResult){
	    	  Intent intent = new Intent(RatingPage.this, MapHome.class);
	          startActivity(intent);
    		}
    	}
    });
  }
  
  
  public void addListenerOnRatingBar() {
	  
		ratingBar = (RatingBar) findViewById(R.id.ratingBar1);
	 
		//if rating value is changed
		ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
			public void onRatingChanged(RatingBar ratingBar, float rating,
				boolean fromUser) {
	 
				ratingString = String.valueOf(rating);
	 
			}
		});
	  }
  
  private void post () {

	if (ratingString.equals("Choose rating:")){
		Toast.makeText(RatingPage.this, "Please rate the previous dash!", Toast.LENGTH_LONG).show();
	}
	
	else{
		postResult = true;

		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", dasherName);
		query.findInBackground(new FindCallback<ParseUser>() {
		  public void done(List<ParseUser> objects, ParseException e) {
		    if (e == null) {
		    	
		    	// getting the info of the user, gets first in the list because list only has one entry
		    	double oldRating = objects.get(0).getDouble("rating");
		    	int oldNumDash = objects.get(0).getInt("dashesComplete");
		    	
		    	int updatedNumDash = oldNumDash + 1;
		    	
		    	double rating = Double.parseDouble(ratingString);
		    	
		    	double updatedRating = (oldRating*oldNumDash + rating) / updatedNumDash;

				Toast.makeText(RatingPage.this, "Thanks for using Dash!", Toast.LENGTH_LONG).show();
		    			    	
		    	// Storing new values into the user
		    	objects.get(0).put("dashesComplete", updatedNumDash);
		    	objects.get(0).put("rating", updatedRating);
		    	objects.get(0).saveInBackground();
		    	
		    	
		    } else {
		        // Something went wrong.
		    	Log.d("RatingPage:", "Failed to change ratings.");
		    }
		  }
		});

	  }
  }
}