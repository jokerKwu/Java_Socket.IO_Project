package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
	private static Database instance =new Database();
	
	
	
	private String driver = "oracle.jdbc.driver.OracleDriver";
	private String url = "jdbc:oracle:thin:@localhost:1521:db";
	private String user = "system";
	private String pw = "1234";
	
	private Connection conn=null;
	private Statement st=null;
	private ResultSet rs= null;
	private Database() {
		
	}
	public static Database getInstance() {
		return instance;
	}
	public Connection getConnection() {
		return conn;
	}
	public void connect() throws Exception{
		if(conn!=null) return;
		try {
			Class.forName(driver);
			conn=DriverManager.getConnection(url,user,pw);
			st=conn.createStatement();
			
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}catch(SQLException e) {
			e.printStackTrace();
		}finally {
			if(rs!=null)rs.close();
			if(st!=null)st.close();
			if(conn!=null)conn.close();
		}
	}
	public void disconnect() {
		if(conn!=null) {
			try {
				conn.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
		conn=null;
	}
}
