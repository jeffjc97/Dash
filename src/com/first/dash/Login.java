package com.first.dash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class Login extends Activity{

    // establishes global variables
	TextView username_text, password_text, error;
	Button submit;
	String password, username;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        
        // finds the corresponding values in the layout file
        username_text = (TextView) findViewById(R.id.login_username);
        password_text = (TextView) findViewById(R.id.login_password);
        error = (TextView) findViewById(R.id.login_failure);
        submit = (Button) findViewById(R.id.login_submit);
        
        // onClick handler for the Login submit
        submit.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
                
                // gets the values in the password and username fields
                password = password_text.getText().toString();
                username = username_text.getText().toString();

                // starts Parse login flow
				ParseUser.logInInBackground(username, password, new LogInCallback() {
						  @Override
						  public void done(ParseUser user, ParseException e) {
						    if (e == null)
						    {
                                // on success
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
						    	Intent openMapHome = new Intent("com.first.dash.MAPHOME");
						    	startActivity(openMapHome);
						    }
						    else
						    {
                                // on failure
						    	error();
						    }
						  }
						});
        	
        	}
        });

    }
    
    // on login error
    private void error(){
    	StringBuilder validationErrorMessage = new StringBuilder(getString(R.string.error_start));
    	// if no username is inputted
        if (username.length() == 0) {
    	    validationErrorMessage.append(getString(R.string.no_username));
    	}
    	// if no password is unputted
    	else if (password.length() == 0) {
    		validationErrorMessage.append(getString(R.string.no_password));
    	}
    	// other failure
        else{
    		validationErrorMessage.append(getString(R.string.login_fail_unknown));
    	}
        // creates notification text
	    Toast.makeText(Login.this, validationErrorMessage.toString(), Toast.LENGTH_LONG)
	        .show();
    }
    
}