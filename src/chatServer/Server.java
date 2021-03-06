package chatServer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class Server {
	
	private DatagramSocket socket;
	private DatagramPacket receivePacket;
	private ExecutorService executorService;
	private static Map<Integer, Object> userList;
	private ServerSocket server;
	private Socket connection;
	private static ObjectInputStream input;
	private static ObjectOutputStream output;
	
	public Server(){
		try{
			socket = new DatagramSocket(54321);
		}catch (SocketException e){
			e.printStackTrace();
		}
		executorService = Executors.newCachedThreadPool();
		
		try{
			server = new ServerSocket(54322);

		}catch (IOException e){
			e.printStackTrace();
		}

		userList = new HashMap<Integer, Object>();
		
		
	}
	
	public synchronized void connectClients(){
		Map<String, Object> message;
		try {
			connection = server.accept();
			output = new ObjectOutputStream(connection.getOutputStream());
			input = new ObjectInputStream(connection.getInputStream());
			
			message = (Map<String, Object>) input.readObject();
			
			if ("login".equals(message.get("type")) || "signUp".equals(message.get("type"))){
				addClient(message, /*eceivePacket, */(String) message.get("type"), connection, input, output);
			}

			
		}catch (IOException | ClassNotFoundException e){
			e.printStackTrace();
		}
		
	}
	
	public void sendResponse(Object message){
		try {
			output.writeObject(message);
			output.flush();
		}catch (IOException e){
			e.printStackTrace();
		}
		
	}
	
	public void receivePacket(){
		Map<String, Object> message;
		
		try{
			byte[] data = new byte[1024];
			receivePacket = new DatagramPacket(data, data.length);
			socket.receive(receivePacket);
		
			message = convertToMap(receivePacket.getData());
			if ("message".equals(message.get("type"))){
				System.out.println(message);
				System.out.println(userList);
				if (userList.get((Integer) message.get("userID")) != null){
					System.out.println("!!!!!");
					System.out.println("packet: " + receivePacket.getPort());
					if (userList.get((Integer) message.get("chatToID")) != null)
						sendPacket((Integer) message.get("userID"), (Integer) message.get("chatToID"), (String) message.get("message"));
					else
						sendAutoPacket((Integer) message.get("chatToID"), (Integer) message.get("userID"), "对方用户不在线，无法收到您的消息！");
				
				}
			}

		}catch(IOException e){
			e.printStackTrace();
		}
		//sendPacket();
		
	}
	
	public void sendAutoPacket(int autoFrom, int to, String m){
		try{
			Map<String, Object> message = new HashMap<String, Object>();
			message.put("time", getTime());
			message.put("message", m);
			message.put("chatToID", to);
			message.put("chatToUsername", ((User) userList.get(to)).getUsername());
			message.put("fromID", autoFrom);
			message.put("fromName", "系统消息");
			
			System.out.println(userList);
			byte[] data = convertToByte(message);
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, 
					((User) userList.get(to)).getAddress(), ((User) userList.get(to)).getPort());
			
			socket.send(sendPacket);
			
		}catch (IOException | NullPointerException e){
			e.printStackTrace();
		}
	}
	
	public void sendPacket(int from, int to, String m){
		try{
			Map<String, Object> message = new HashMap<String, Object>();
			message.put("time", getTime());
			message.put("message", m);
			message.put("chatToID", to);
			message.put("chatToUsername", ((User) userList.get(to)).getUsername());
			message.put("fromID", from);
			message.put("fromName", ((User) userList.get(from)).getUsername());
			
			System.out.println(userList);
			byte[] data = convertToByte(message);
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, 
					((User) userList.get(to)).getAddress(), ((User) userList.get(to)).getPort());
			
			socket.send(sendPacket);
			
		}catch (IOException | NullPointerException e){
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
	
	private void addClient(Map<String, Object> userInfo, String type, Socket connection,
			ObjectInputStream input, ObjectOutputStream output){
		Map<String, Object> message = new HashMap<String, Object>();
		try{
			if ("login".equals(type))
				message = Database.userLogin(userInfo);
			else if ("signUp".equals(type))
				message = Database.AddUser(userInfo);
			else
				throw new Exception();
			System.out.println("000dsaddqw");
			if ((Integer) message.get("messageCode") == Code.SUCCESS){
				User user = new User(connection, input, output, userInfo, (Integer) message.get("userID"));
				executorService.execute(user);
				
				user.setAddress(connection.getInetAddress());
				user.setPort((Integer) userInfo.get("port"));
				System.out.print("connection: " + connection.getPort());

				System.out.println(message);
				userList.put((Integer) message.get("userID"), user);
				System.out.println(userList);

			}

		}catch(MySQLIntegrityConstraintViolationException e0){
			e0.printStackTrace();
			message.put("messageCode", Code.DUP_USERNAME);
		}catch (SQLException e1){
			e1.printStackTrace();
			message.put("messageCode", Code.SQL_EXCEPTION);
		}catch (Exception e2){
			e2.printStackTrace();
			message.put("messageCode", Code.UNKNOW_ERROR);
		}

		System.out.println("111dsaddqw");
		sendResponse(message);
		connection = null;
		
	}
	
	public static void logout(int id){
		userList.remove(id);
		System.out.println(userList);
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
		
		new Thread(new Runnable(){
			public void run(){
				while (true){
					server.connectClients();
				}
			}
		}).start();;
		
	}
}
