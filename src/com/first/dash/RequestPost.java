package com.first.dash;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

//Creates the RequestPost class that can be stored in parse
@ParseClassName("Posts")
public class RequestPost extends ParseObject{
  	
  // functions for setting and retrieving values from the object
  public void setItem(String value) {
	  put("item", value);
  }
  public String getItem(){
	  return getString("item");
  }
  
  public void setUsername(String value) {
	  put("username", value);
  }
  public String getUsername(){
	  return getString("username");
  }
  
  public void setPayment(String value) {
	  put("payment", value);
  }
  public String getPayment(){
	  return getString("payment");
  }
  
  public void setTime(String value) {
	  put("time", value);
  }
  public String getTime(){
	  return getString("time");
  }
  
  public void setItemCost(String value) {
	  put("itemCost", value);
  }
  public String getItemCost(){
	  return getString("itemCost");
  }
  
  public void setExtraInfo(String value) {
	  put("extraInfo", value);	    
  }
  public String getExtraInfo(){
	  return getString("extraInfo");
  }
  
  public void setStatus(String value) {
	  put("status", value);    
  }
  public String getStatus(){
	  return getString("status");
  }
 
  public void setPostTime(long value) {
	  put("postTime", value);    
  }
  public long getPostTime(){
	  return getLong("postTime");
  }  
  public void setDeleteTime(long value) {
	  put("deleteTime", value);    
  }
  public long getDeleteTime(){
	  return getLong("deleteTime");
  }   
  public void setDasher(String value) {
	  put("Dasher", value);    
  }
  public String getDasher(){
	  return getString("Dasher");
  }     
  
  
  public void setAddressOne(String value) {
	  put("address1", value);
  }
  public String getAddressOne(){
	  return getString("address1");
  }
  
  public void setAddressTwo(String value) {
	  put("address2", value);
  }
  public String getAddressTwo(){
	  return getString("address2");
  }

  public void setCity(String value) {
	  put("city", value);
  }
  public String getCity(){
	  return getString("city");
  }
  
  public void setState(String value) {
	  put("state", value);
  }
  public String getState(){
	  return getString("state");
  }
  
  public void setZipcode(String value) {
	  put("zipcode", value);
  }
  public String getZipcode(){
	  return getString("zipcode");
  }
  
  public void setDate(String value) {
	  put("fulfillTime", value);
  }
  public long getDate(){
	  return getLong("fulfillTime");
  }  
  
  public ParseUser getUser() {
    return getParseUser("user");
  }
 
  public void setUser(ParseUser value) {
    put("user", value);
  }
 
  public ParseGeoPoint getLocation() {
    return getParseGeoPoint("location");
  }
 
  public void setLocation(ParseGeoPoint value) {
    put("location", value);
  }
 
  public void setTargetLocation(ParseGeoPoint value) {
	put("targetLocation", value);
  }
  
  public ParseGeoPoint getTargetLocation() {
	  return getParseGeoPoint("targetLocation");
  }
  
  
  public static ParseQuery<RequestPost> getQuery() {
    return ParseQuery.getQuery(RequestPost.class);
  }
  
}
