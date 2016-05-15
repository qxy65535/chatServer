package chatServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
		ResultSetMetaData data = resultSet.getMetaData();
		
		Map<String, Object> message = new HashMap<String, Object>();
		if (resultSet.next()){
			message.put("messageCode", Code.SUCCESS);
			message.put("userID", resultSet.getInt("userID"));
			resultSet = statement.executeQuery("SELECT userName FROM user WHERE userName<>'" + username + "'");
			ArrayList<String> clients = new ArrayList<String>();
			while (resultSet.next()){
				clients.add(resultSet.getString(1));
				System.out.println(resultSet.getString(1));
			}
			message.put("clients", clients);
			//System.out.println(resultSet.getObject(1));
		}
		else {
			message.put("messageCode", Code.USER_INFO_ERROR);
		}
		return message;
	}
	
	public static void AddUser(Map<String, Object> map) throws SQLException{
		String name = (String)map.get("userName");
		String password = (String)map.get("password");

		statement.execute("INSERT INTO user SET userName='" + name +"',"+ "password='" + password + "'");
		
	}

}
