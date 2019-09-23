package OracleDAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.plaf.synth.SynthSplitPaneUI;

public class OracleDBConnection {

	private static OracleDBConnection instance = new OracleDBConnection();

	private static String driver = "oracle.jdbc.driver.OracleDriver";
	private static String url = "jdbc:oracle:thin:@localhost:1521:db";
	private static String user = "system";
	private static String pw = "1234";

	private static Connection conn = null;
	private static Statement st = null;
	private static ResultSet rs = null;
	
	static {
		try {
			Class.forName(driver);
			System.out.println("드라이버 로딩 성공");
			
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		}
	
	
	public OracleDBConnection() {
		
	}

	//디비 객체 가져오는 메소드 (싱글톤 사용)
	public static OracleDBConnection getInstance() {
		return instance;
	}
	
	//디비 접속 하는 메소드
	public Connection getConnection() throws SQLException {
		if(conn!=null) 
			return conn;
		else { 
			try {
			conn=DriverManager.getConnection(url,user,pw);
			st=conn.createStatement();

			System.out.println("디비 접속 성공");
			}catch(SQLException e) {
				System.out.println("디비 접속 실패");
				e.printStackTrace();
			}
			return conn;
		}
	}

	//디비 접속 해제 메소드
	public void getDisconnect() {

		try {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (conn != null)
				conn.close();
			System.out.println("접속 해제 완료");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		conn = null;
		rs = null;
		st = null;
	}
}
