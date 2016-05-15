package chatServer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
	
	private DatagramSocket socket;
	private DatagramPacket receivePacket;
	private ExecutorService executorService;
	private Map<String, Object> userList;
	
	public Server(){
		try{
			socket = new DatagramSocket(12345);
		}catch (SocketException e){
			e.printStackTrace();
		}
		executorService = Executors.newCachedThreadPool();
		userList = new HashMap<String, Object>();
		
	}
	
	public void receivePacket(){
		Map<String, Object> message;
		
		try{
			byte[] data = new byte[1024];
			receivePacket = new DatagramPacket(data, data.length);
			socket.receive(receivePacket);
		
			message = convertToMap(receivePacket.getData());
			if ("login".equals(message.get("type"))){
				addClient(message, receivePacket);
			}
			else if ("message".equals(message.get("type"))){
				if (userList.get((String) message.get("username")) != null){
					sendPacket((String) message.get("username"), (String) message.get("chatTo"), (String) message.get("message"));
				}
			}
//			System.out.println("username:" + message.get("userName"));
//			System.out.println("password:" + message.get("password"));

		}catch(IOException e){
			e.printStackTrace();
		}
		//sendPacket();
		
	}
	
	public void sendPacket(String from, String to, String m){
		try{
			Map<String, Object> message = new HashMap<String, Object>();
			message.put("time", getTime());
			message.put("message", m);
			message.put("chatTo", to);
			message.put("from", from);
			
			byte[] data = convertToByte(message);
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, 
					((User) userList.get(to)).getAddress(), ((User) userList.get(to)).getPort());
			
			socket.send(sendPacket);
			
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public void sendResponseMessage(Map<String, Object> message){
		
		try{

			byte[] data = convertToByte(message);
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, 
					receivePacket.getAddress(), receivePacket.getPort());
			
			socket.send(sendPacket);
			
			//System.out.println(code + "     " + String.valueOf(code));
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private Map<String, Object> convertToMap(byte[] data) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			ObjectInputStream ois = new ObjectInputStream(bais);
		    Map<String, Object> result = (Map<String, Object>) ois.readObject();
		    return result;
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return null;
	}
	
	public byte[] convertToByte(Map<String, Object> map) {
	    try {		      
	    	ByteArrayOutputStream b = new ByteArrayOutputStream();
		    ObjectOutputStream ois = new ObjectOutputStream(b);
		    ois.writeObject(map);
		    byte[] temp = b.toByteArray();
		    return temp;
		      
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
		  return null;
	}
	
	private void addClient(Map<String, Object> userInfo, DatagramPacket receivePacket){
		Map<String, Object> message;
		try{
			message = Database.userLogin(userInfo);
			if ((int) message.get("messageCode") == Code.SUCCESS){
				User user = new User(socket, userInfo, (int) message.get("userID"));
				executorService.execute(user);
				
				user.setAddress(receivePacket.getAddress());
				user.setPort(receivePacket.getPort());

				userList.put((String) userInfo.get("username"), user);

			}
			
			sendResponseMessage(message);
			
		}catch (SQLException e){
			e.printStackTrace();
			message = new HashMap<String, Object>();
			message.put("messageCode", Code.SQL_EXCEPTION);
			sendResponseMessage(message);
		}
		
	}
	
	public String getTime(){
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
	}
	
	public static void main(String[] args){
		
		Database data = new Database();
		Server server = new Server();
		
		new Thread(new Runnable(){
			public void run(){
				while (true)
					server.receivePacket();
			}
		}).start();
		
	}
}
