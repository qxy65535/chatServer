package chatServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class User implements Runnable{
	
	private String username;
	private String password;
	private int userID;
	private Socket socket;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	//private DatagramPacket receivePacket;
	private InetAddress address;
	private int port;
	Map<String, Object> message;
	
	public User(Socket socket, ObjectInputStream input, ObjectOutputStream output, Map<String, Object> userInfo, int userID){
		this.socket = socket;
		this.input = input; 
		this.output = output;
		username = (String) userInfo.get("username");
		password = (String) userInfo.get("password");
		this.userID = userID;
	}
	
	public void run(){
		while (true){
			try{
				message = (Map<String, Object>) input.readObject();
				if ("addFriend".equals(message.get("type"))){
					addFriend(message /*receivePacket, */);
				}
				else if ("logout".equals(message.get("type"))){
					output.close();
					input.close();
					socket.close();
					Server.logout(userID);
					break;
				}
			}catch (ClassNotFoundException | IOException e){
				e.printStackTrace();
			}
		}
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
	
	private void addFriend(Map<String, Object> user /*DatagramPacket receivePacket, */){
		Map<String, Object> message = new HashMap<String, Object>();
		
		try{
			message = Database.AddFriend(user);
			
			//sendResponseMessage(message);
			//sendResponse(message);
		}catch (SQLException e){
			e.printStackTrace();
			//message = new HashMap<String, Object>();
			message.put("messageCode", Code.SQL_EXCEPTION);
			//sendResponseMessage(message);
		}finally {
			message.put("type", "addFriendResponse");
			sendResponse(message);
		}
	}
	
	public void sendResponse(Object message){
		try {
			//output = new ObjectOutputStream(connection.getOutputStream());
			output.writeObject(message);
			output.flush();
		}catch (IOException e){
			e.printStackTrace();
		}
		
	}
}


