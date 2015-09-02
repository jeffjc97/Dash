package com.first.dash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class Register extends Activity {

    // establishes global variables
	TextView error;
	EditText username_edit, password_edit, confirmation_edit;
	Button submit;
	String password, username, confirmation;
	boolean validationError;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);
        
        // finds variables inside of the layout file
        username_edit = (EditText) findViewById(R.id.register_username);
        password_edit = (EditText) findViewById(R.id.register_password);
        confirmation_edit = (EditText) findViewById(R.id.register_confirm);
        error = (TextView) findViewById(R.id.register_failure);   
        submit = (Button) findViewById(R.id.register_submit);
        
        // onClick listener on the register submit button
        submit.setOnClickListener(new View.OnClickListener(){
        	@Override
        	public void onClick(View v){
        		signup1();
        		if (!validationError){
        			signup2();
        		}
        		
        	}
        });

    }


    
    private void signup1() {
    	// initially has no validation error
        validationError = false;
        
        // gets the values of username, password and confirmation
        password = password_edit.getText().toString().trim();
        username = username_edit.getText().toString().trim();
        confirmation = confirmation_edit.getText().toString().trim();  
    	
    	// builds the error message
        StringBuilder validationErrorMessage = new StringBuilder(getString(R.string.error_start));
		  // if an improper username has been submitted
          if (username.length() < 5) {
		    validationError = true;
		    validationErrorMessage.append(" Username must be at least 5 characters");
		  }
		  // if an improper password has been submitted
		  if (password.length() < 5) {
		      // if there is already an error, append a space
	          validationError = true;
		      validationErrorMessage.append(" Password must be at least 5 characters");
		  }      
          // if password and confirmation don't match
		  if (!password.equals(confirmation)) {
		    if (validationError) {
		      validationErrorMessage.append(getString(R.string.space));
		    }
		    validationError = true;
		    validationErrorMessage.append(getString(R.string.mismatch_confirm));
		  }
		  // ends the error message
          validationErrorMessage.append(getString(R.string.period));
          // shows error message if there is one


		  if (validationError) {
			    Toast.makeText(Register.this, validationErrorMessage.toString(), Toast.LENGTH_LONG)
			        .show();
			    return;
			  }

    	
    }
    public void signup2(){
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", username);
		query.getFirstInBackground(new GetCallback<ParseUser>() {
		  public void done(ParseUser object, ParseException e) {
		    if (e == null){
		    	// on successful query
		    	Toast.makeText(Register.this, "This username alraedy exists", Toast.LENGTH_LONG).show();
		    }
		    // on query failure
		    else {
		    	
				ParsePush.subscribeInBackground(username, new SaveCallback() {
					  @Override
					  public void done(ParseException e) {
					    if (e == null) {
					      Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
					    } else {
					      Log.e("com.parse.push", "failed to subscribe for push", e);
					    }
					  }
					});
		    	// Set up a new Parse user
		    	ParseUser user = new ParseUser();
		    	user.setUsername(username);
		    	user.setPassword(password);
		    	user.put("dashesComplete", 0);
		    	user.put("rating", 0);
		    	// Call the Parse signup method
			 	user.signUpInBackground();
			 	 
			 	Intent openMapHome = new Intent("com.first.dash.MAPHOME");
			 	startActivity(openMapHome);  
			  	Log.d(username, e.toString());
		    }
		  }
		});
    }
}
    