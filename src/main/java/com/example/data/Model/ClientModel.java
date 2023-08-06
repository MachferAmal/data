package com.example.data.Model;

public class ClientModel {

    private int  port;
    private String addressIp;
    private String path;
    public ClientModel( int port, String path,String addressIp) {
    	 this.addressIp = addressIp;
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
	public String getAddressIp() {
		return addressIp;
	}
	public void setAddressIp(String addressIp) {
		this.addressIp = addressIp;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
    
	
}
