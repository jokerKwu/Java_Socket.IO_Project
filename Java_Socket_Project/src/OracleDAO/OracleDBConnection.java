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
			System.out.println("����̹� �ε� ����");
			
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		}
	
	
	public OracleDBConnection() {
		
	}

	//��� ��ü �������� �޼ҵ� (�̱��� ���)
	public static OracleDBConnection getInstance() {
		return instance;
	}
	
	//��� ���� �ϴ� �޼ҵ�
	public Connection getConnection() throws SQLException {
		if(conn!=null) 
			return conn;
		else { 
			try {
			conn=DriverManager.getConnection(url,user,pw);
			st=conn.createStatement();

			System.out.println("��� ���� ����");
			}catch(SQLException e) {
				System.out.println("��� ���� ����");
				e.printStackTrace();
			}
			return conn;
		}
	}

	//��� ���� ���� �޼ҵ�
	public void getDisconnect() {

		try {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (conn != null)
				conn.close();
			System.out.println("���� ���� �Ϸ�");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		conn = null;
		rs = null;
		st = null;
	}
}
