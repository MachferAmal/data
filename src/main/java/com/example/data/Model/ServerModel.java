package com.example.data.Model;



public class ServerModel {
	//@Min(value = 1, message = "Le port doit être supérieur à 0")
    private int  port;
	//@NotEmpty(message = "Le chemin (path) ne peut pas être vide")
    private String path;
    public ServerModel( int port, String path) {
    	 
          this.port = port;
          this.path = path;
		// TODO Auto-generated constructor stub
	}
	
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
    
    
    
    
	
}