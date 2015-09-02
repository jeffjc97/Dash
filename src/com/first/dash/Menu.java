package com.first.dash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Menu extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		// creates global variables
		Button login, register;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);

		// finds the buttons in the layout files
        login = (Button) findViewById(R.id.menu_login);
        register = (Button) findViewById(R.id.menu_register);
        
        // onclick listener for login
        login.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
		    	// goes to the login page
		    	Intent openLogin = new Intent("com.first.dash.LOGIN");
		    	startActivity(openLogin);
				  }
				});
	
        // onclick listener for register
        register.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
		    	// goes to the register page
		    	Intent openRegistration = new Intent("com.first.dash.REGISTER");
		    	startActivity(openRegistration);
				  }
				});		
		
	}
	

}