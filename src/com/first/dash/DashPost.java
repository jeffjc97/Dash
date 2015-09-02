package com.first.dash;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

// Creates the DashPost class that can be stored in parse
@ParseClassName("DashPost")
public class DashPost extends ParseObject{
  	
  // functions for setting and retrieving values from the object
  public void setInstallationId (String value){
	  put("installationId", value);
  }
  
  public void setBoolean (boolean value){
	  put("Request", value);
  }
  public boolean  getBoolean (){
	  return getBoolean("Request");
  }
  
  public String getAsker() {
    return getString("Asker");
  }
	 
  public void setAsker(String value) {
	  put("Asker", value);
  }
  public String getDasher() {
	    return getString("Dasher");
	  }
		 
  public void setDasher(String value) {
	  put("Dasher", value);
  }
 
  public static ParseQuery<DashPost> getQuery() {
    return ParseQuery.getQuery(DashPost.class);
  }
  
}
