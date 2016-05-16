package chatServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

public class User implements Runnable{
	
	private String username;
	private String password;
	private int userID;
	private DatagramSocket socket;
	private DatagramPacket receivePacket;
	private InetAddress address;
	private int port;
	
	public User(DatagramSocket socket, Map<String, Object> userInfo, int userID){
		this.socket = socket;
		username = (String) userInfo.get("username");
		password = (String) userInfo.get("password");
		this.userID = userID;
	}
	
	public void run(){
		
	}
	
	public int getID(){
		return userID;
	}
	
	public String getUsername(){
		return username;
	}
	
	public void setAddress(InetAddress address){
		this.address = address;
	}
	
	public InetAddress getAddress(){
		return address;
	}
	
	public void setPort(int port){
		this.port = port;
	}
	
	public int getPort(){
		return port;
	}
}


