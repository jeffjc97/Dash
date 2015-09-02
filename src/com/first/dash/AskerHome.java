package com.first.dash;

import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;



public class AskerHome extends Activity {
	
		// establishes globabl variables 
	  	TextView textViewTime;
	  	String timeString, objectId, dasherString;	  
		int seconds;
		long deadline, currentTime;

	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.asker_home);
	    
	    if (ParseUser.getCurrentUser() == null){
	    	Intent redirect = new Intent("com.first.dash.MENU");
	    	startActivity(redirect);
	    }
	    
	    // finds the timer in the layout file
	    textViewTime = (TextView) findViewById(R.id.textViewTime);
	    
		// query for pending requests for the current user
		ParseQuery<RequestPost> query = RequestPost.getQuery();
		query.whereEqualTo("status", "Pending");
		query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
		query.getFirstInBackground(new GetCallback<RequestPost>() {
		  public void done(RequestPost object, ParseException e) {
		    if (e == null) {
		    	// on success
		    	objectsWereRetrievedSuccessfully(object);
		    } else {
		      // on failure
		      Log.d("RequestMoreInfo:", "Failed to retrieve the object.");
		    }
		  }
		});
		
		// finds dashButton in the layout file
		Button completeButton = (Button) findViewById(R.id.btn_confirm);
	    
		// onClick handler for dash
	    completeButton.setOnClickListener(new OnClickListener(){
	    	public void onClick(View v){
	    		
	    		setComplete();   		
	            Intent intent = new Intent(AskerHome.this, RatingPage.class);
	            intent.putExtra("dasher", dasherString);
	    		startActivity(intent);
	    	}
	    });
		
		
	    
	  }
	  
	  
	  
	  
	  
	  	// creates the timer class
		@TargetApi(Build.VERSION_CODES.GINGERBREAD)
		@SuppressLint("NewApi")
		public class CounterClass extends CountDownTimer {
	
			public CounterClass(long millisInFuture, long countDownInterval) {
				super(millisInFuture, countDownInterval);
			}
	
			@SuppressLint("NewApi")
			@TargetApi(Build.VERSION_CODES.GINGERBREAD)
			@Override
			public void onTick(long millisUntilFinished) {
				
				// establishes the format of the timer
				long millis = millisUntilFinished;
				String hms = String.format("%02d:%02d",
						TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
						TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
				// prints time in log for error checking
				System.out.println(hms);
				// constantly updates the user display
				textViewTime.setText(hms);
			}
	
			@Override
			public void onFinish() {
				// After the timer has run out
				textViewTime.setText("Sorry, Dasher failed.");
				
				// Sets the status of the request in Parse to complete once the user checks
				// the status and the timer has finished
				ParseQuery<RequestPost> timeEnd = RequestPost.getQuery();
				timeEnd.whereEqualTo("objectId", objectId);
				timeEnd.getFirstInBackground(new GetCallback<RequestPost>() {
				  public void done(RequestPost object, ParseException e) {
				    if (e == null) {
				    	// updates the object
				    	object.put("status", "Complete");
				    	object.saveInBackground();
				    	
				    } else {
				      // logs the error
				      Log.d("RequestMoreInfo:", "Failed to end the time.");
				    }
				  }
				});
			}
			
			
			
		}
		
		// occurs when the information for the layout has been retreived successfully
		private void objectsWereRetrievedSuccessfully(RequestPost post){
			  
			  
			// finds the fields to be updated
			TextView dasher = (TextView) findViewById(R.id.asker_home_username);             
			TextView payment = (TextView) findViewById(R.id.asker_home_payment);        
			TextView item = (TextView) findViewById(R.id.asker_home_item);        
			TextView itemCost = (TextView) findViewById(R.id.asker_home_cost);   
			TextView time = (TextView) findViewById(R.id.asker_home_time);
			
			// updates the fields
			dasherString = post.getDasher().toString();
			if (dasherString != null){
				dasher.setText("Dasher: " + post.getDasher().toString());
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
			if (post.getObjectId() != null){
				objectId = post.getObjectId();
			}	
			deadline = post.getDate();			
								
			
			
			// gets the time value			
			currentTime = System.currentTimeMillis() / 1000L;

			//finds the time difference between deadline and current time
			seconds = (int) (deadline - currentTime);
			
			// calculates minutes remaining before done
			int mins = (int) seconds / 60;
			int displaySec = seconds - 60 * mins;
			
			// converts time data to strings
			String minsString = Integer.toString(mins);
			String displaySecString = Integer.toString(displaySec);
			
			// starts the timer
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
		
			
		  }
		
	  public void setComplete()
	  {
		  	ParseQuery<RequestPost> queryTime = RequestPost.getQuery();
			queryTime.whereEqualTo("objectId", objectId);
			queryTime.getFirstInBackground(new GetCallback<RequestPost>() {
			  public void done(RequestPost object, ParseException e) {
			    // on successful query
			    if (e == null) {
			    	// updates the request
			    	object.put("status", "Complete");
			    	// saves the object in parse
			    	object.saveInBackground();
			    	
			    } 
			    // on query failure
			    else {
			      Log.d("RequestMoreInfo:", "Failed to complete.");
			    }
			  }
			});	  
	  }
		
}