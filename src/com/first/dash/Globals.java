package com.first.dash;

public class Globals {
	
	private static Globals instance;
	private boolean isLoggedIn;
	  
	public static synchronized Globals getInstance(){
		if(instance==null){
			instance=new Globals();
		}
		return instance;
	}
	  
	public boolean checkLogin() {
		return isLoggedIn;
	}
	  
	public void setLogin(boolean update) {
		this.isLoggedIn = update;
	}
}
