package com.first.dash;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

// defines the ParseClassName
@ParseClassName("DashMessage")
public class DashMessage extends ParseObject{
  	
  // functions for setting and retreiving information from the post
  public void setInstallationId(String value) {
	  put("installationId", value);
  }
  public String getInstallationId(){
	  return getString("installationId");
  }
  
  public void setAsker(String value) {
	  put("Asker", value);
  }
  public String getAsker(){
	  return getString("Asker");
  }
  
  public void setMessage(String value) {
	  put("Message", value);
  }
  public String getMessage(){
	  return getString("payment");
  }
  
  // establishes query method for RequestPost
  public static ParseQuery<DashMessage> getQuery() {
    return ParseQuery.getQuery(DashMessage.class);
  }
  
}
