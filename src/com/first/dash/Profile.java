package com.first.dash;

import java.util.List;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Profile extends BaseActivity {
	
    // establishes global variables
	String username;
	//double rating;
	//int numDashes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
    setContentView(R.layout.profile_page); 
    super.onCreateDrawer();
    
    // queries for the user
    username = ParseUser.getCurrentUser().getUsername();
    
	ParseQuery<ParseUser> query = ParseUser.getQuery();
	query.whereEqualTo("username", username);
	query.findInBackground(new FindCallback<ParseUser>() {
	  public void done(List<ParseUser> objects, ParseException e) {
	    if (e == null) {
	    	
	    	// finds the fields to be updated
	    	TextView username = (TextView) findViewById(R.id.profile_page_username);             
	    	TextView rating = (TextView) findViewById(R.id.profile_page_rating);
	    	TextView numDashes = (TextView) findViewById(R.id.profile_page_num_dashes);
	    	
	    	// updates the fields
	    	username.setText("Username: " + objects.get(0).getUsername().toString());		
	    	rating.setText("Rating: " + String.format("%.2f",objects.get(0).getDouble("rating")));
	    	numDashes.setText("Completed Dashes: " + objects.get(0).getInt("dashesComplete"));    	
	    	
	    } else {
	        // Something went wrong.
	    	Log.d("Profile:", "Failed to retrieve information.");
	    }
	  }
	});
  
    
  }

	
	
	
	
	
}
