package OracleDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OracleDAO {
	
	Statement st=null;
	PreparedStatement pstmt=null;
	ResultSet rs=null;
	private static OracleDAO instance = new OracleDAO();

	//�޼ҵ� ���� DAO_�������̺��_(CRUD)_�޼ҵ��
	//��� ��ü �������� �޼ҵ� (�̱��� ���)
	public static OracleDAO getInstance() {
		return instance;
	}
	
	//�������̵� �� �������� �޼ҵ�
	public void DAO_select_UserId(Connection conn ,String id) throws SQLException {
		try {
			
			String query="insert into user_tbl values(AUTO_INCREMENT.nextval,?)";
			
			pstmt=conn.prepareStatement(query);
			
			pstmt.setString(1, id);
			pstmt.executeUpdate();
			st=conn.createStatement();
			rs=st.executeQuery("select * from user_tbl");
			while(rs.next()) {
				int no=rs.getInt("user_id");
				String name=rs.getString("username");
				System.out.println("num : "+no+" name : "+name);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {

			rs.close();
		
			st.close();
		}
	}
	
	// ��ȭ �����ϴ� �޼ҵ�
	public void DAO_insert_Reply(Connection conn,int user_from,int user_to,String reply) throws SQLException {
		try {
			String query1="insert into conversation_tbl values(auto_increment.nextval,?,?,'2019-09-23')";
			pstmt=conn.prepareStatement(query1);
			pstmt.setInt(1, user_from);
			pstmt.setInt(2, user_to);
			pstmt.executeUpdate();
			
			st=conn.createStatement();
			String query2="select c_id from conversation_tbl where rownum=1 and (user_from=? and user_to=?) order by c_id desc";
			pstmt=conn.prepareStatement(query2);
			pstmt.setInt(1, user_from);
			pstmt.setInt(2, user_to);
			
			rs=pstmt.executeQuery();
			
			int c_id_fk=-1;
			while(rs.next()) {
				c_id_fk=rs.getInt(1);
				break;
			}
			
			String query3="insert into conversation_reply_tbl values(auto_increment.nextval,?,?,'2019-09-24',?)";
			pstmt=conn.prepareStatement(query3);
			pstmt.setString(1, reply);
			pstmt.setInt(2, user_from);
			pstmt.setInt(3, c_id_fk);
			
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			rs.close();
			st.close();
		}
	}
	
	// ��ȭ ������ ���� �������� �޼ҵ�
	public String DAO_select_lastReply(Connection conn,int user_id) throws SQLException{
		String query="select reply from conversation_reply_tbl where rownum=1 and user_id_fk=? order by cr_id desc";
		pstmt=conn.prepareStatement(query);
		pstmt.setInt(1, user_id);
		
		rs=pstmt.executeQuery();
		String answer="";

		while(rs.next()) {
			answer=rs.getString(1);
			break;
		}
		System.out.println(answer);
		
		return answer;
	}
	
	// ��ȭ�ִ��� üũ�ϴ� �޼ҵ�
	public boolean DAO_select_conversationCheck(Connection conn,String query) {
		boolean check=false;
		
		
		return check;
	}
	
}
