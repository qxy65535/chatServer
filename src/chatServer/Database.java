package chatServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class Database {

	private final String DATABASE_URL = "jdbc:mysql://localhost/java_chat";
	private final String USERNAME = "root";
	private final String PASSWORD = "admin";
	private Connection connection;
	private static Statement statement;
	
	
	public Database(){
		try{
			connection = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
			statement = connection.createStatement();
		}catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	public static Map<String, Object> userLogin(Map<String, Object> userInfo) throws SQLException{
		String username = (String) userInfo.get("username");
		String password = (String) userInfo.get("password");
		ResultSet resultSet;
		System.out.println(username +"  "+ password);
		resultSet = statement.executeQuery("SELECT * FROM user WHERE userName='" + username + "' AND password='" + password + "'");
//		ResultSetMetaData data = resultSet.getMetaData();
		
		Map<String, Object> message = new HashMap<String, Object>();
		if (resultSet.next()){
			message.put("messageCode", Code.SUCCESS);
			message.put("userID", resultSet.getInt("userID"));
			
			getFriendList(username, message);
			
			//System.out.println(message.get("friend"));
			
//			resultSet = statement.executeQuery("SELECT userName FROM user WHERE userName<>'" + username + "'");
//			ArrayList<String> clients = new ArrayList<String>();
//			while (resultSet.next()){
//				clients.add(resultSet.getString(1));
//				System.out.println(resultSet.getString(1));
//			}
//			message.put("clients", clients);
			//System.out.println(resultSet.getObject(1));
		}
		else {
			message.put("messageCode", Code.USER_INFO_ERROR);
		}
		return message;
	}
	
	private static void getFriendList(String username, Map<String, Object> message) throws SQLException{
		ResultSet resultSet;
		ArrayList<Integer> friendID = new ArrayList<Integer>();
		ArrayList<String> friendName = new ArrayList<String>();
		
		resultSet = statement.executeQuery("SELECT friendID,friendName FROM user,friend"
				+ " WHERE userName='" + username + "' AND user.userID=friend.userID");
		
		while (resultSet.next()){
			friendID.add(resultSet.getInt(1));
			friendName.add(resultSet.getString(2));
			//System.out.println(resultSet.getString(1));
		}
		message.put("friendID", friendID);
		message.put("friendName", friendName);
	}
	
	public static Map<String, Object> AddFriend(Map<String, Object> info) throws SQLException{
		String name = (String)info.get("friendName");
		
		ResultSet resultSet;
		resultSet = statement.executeQuery("SELECT userName,userID FROM user WHERE userName='" + name +"'");
		
		Map<String, Object> friendInfo = new HashMap<String, Object>();
		if (resultSet.next()){
			int id = resultSet.getInt(2);
			friendInfo.put("username", name);
			friendInfo.put("userID", id);
			friendInfo.put("messageCode", Code.SUCCESS);
			statement.execute("INSERT INTO friend VALUES("
					+ info.get("userID") + "," + id + ",'" + name + "')");
		}
		else {
			friendInfo.put("messageCode", Code.USER_INFO_ERROR);
		}
		
		return friendInfo;
	}
	
	
	public static Map<String, Object> AddUser(Map<String, Object> userInfo) throws SQLException, MySQLIntegrityConstraintViolationException{
		String name = (String)userInfo.get("username");
		String password = (String)userInfo.get("password");

		statement.execute("INSERT INTO user(userName,password)"
				+ "VALUES('" + name + "','" + password + "')");
		
		return userLogin(userInfo);
	}
	

}
