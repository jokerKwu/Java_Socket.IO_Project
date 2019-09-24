package OracleDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OracleDAO {

	Statement st = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	private static OracleDAO instance = new OracleDAO();

	// �޼ҵ� ���� DAO_�������̺��_(CRUD)_�޼ҵ��
	// ��� ��ü �������� �޼ҵ� (�̱��� ���)
	public static OracleDAO getInstance() {
		return instance;
	}

	// �������̵� �� �������� �޼ҵ�
	public int DAO_select_UserId(Connection conn, String id) throws SQLException {
		int user_id = -1;
		try {
			//���̵� �ִ��� üũ�ϰ�
			String query = "select * from user_tbl where username=?";
			pstmt=conn.prepareStatement(query);
			pstmt.setString(1, id);
			rs=pstmt.executeQuery();
			while (rs.next()) {
				user_id = rs.getInt("user_id");
			}
			if (user_id != -1) {
				return user_id;
			} else {
				System.out.println("�׽�Ʈ 1");
				String query1 = "insert into user_tbl values(AUTO_INCREMENT.nextval,?)";

				pstmt = conn.prepareStatement(query1);

				pstmt.setString(1, id);
				pstmt.executeUpdate();
				
				String query2="select * from user_tbl where username=?";
				pstmt=conn.prepareStatement(query2);
				pstmt.setString(1, id);
				rs=pstmt.executeQuery();
				while(rs.next()) {
					user_id=rs.getInt(1);
					break;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(rs!=null)
				rs.close();
			if(pstmt!=null) pstmt.close();
			if(st!=null) st.close();
		}
		return user_id;
	}

	// ��ȭ �����ϴ� �޼ҵ�
	public void DAO_insert_Reply(Connection conn, int user_from, int user_to, String reply) throws SQLException {
		try {
			String query1 = "insert into conversation_tbl values(auto_increment.nextval,?,?,'2019-09-23')";
			pstmt = conn.prepareStatement(query1);
			pstmt.setInt(1, user_from);
			pstmt.setInt(2, user_to);
			pstmt.executeUpdate();

			st = conn.createStatement();
			String query2 = "select c_id from conversation_tbl where rownum=1 and (user_from=? and user_to=?) order by c_id desc";
			pstmt = conn.prepareStatement(query2);
			pstmt.setInt(1, user_from);
			pstmt.setInt(2, user_to);

			rs = pstmt.executeQuery();

			int c_id_fk = -1;
			while (rs.next()) {
				c_id_fk = rs.getInt(1);
				break;
			}

			String query3 = "insert into conversation_reply_tbl values(auto_increment.nextval,?,?,'2019-09-24',?)";
			pstmt = conn.prepareStatement(query3);
			pstmt.setString(1, reply);
			pstmt.setInt(2, user_from);
			pstmt.setInt(3, c_id_fk);

			pstmt.executeUpdate();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			rs.close();
			st.close();
		}
	}

	// ��ȭ ������ ���� �������� �޼ҵ�
	public String DAO_select_lastReply(Connection conn, int user_id) throws SQLException {
		String query = "select reply from conversation_reply_tbl where rownum=1 and user_id_fk=? order by cr_id desc";
		pstmt = conn.prepareStatement(query);
		pstmt.setInt(1, user_id);

		rs = pstmt.executeQuery();
		String answer = null;

		while (rs.next()) {
			answer = rs.getString(1);
			break;
		}

		return answer;
	}

	// Ư�� ��ȭ�� �����Ѵ�.
	public void DAO_insert_saveConversation(Connection conn, int user_id) throws SQLException {
		String reply = null;
		int cr_id = 0;
		// ���� ���̵� �ش�Ǵ� ������ ������ �ҷ�����
		String query1 = "select cr_id,reply from conversation_reply_tbl where rownum=1 and user_id_fk=? order by cr_id desc";
		pstmt = conn.prepareStatement(query1);
		pstmt.setInt(1, user_id);

		rs = pstmt.executeQuery();
		while (rs.next()) {
			cr_id = rs.getInt(1);
			reply = rs.getString(2);
			break;
		}

		// �ش� ������ ������ ��ȭ cr_id�� ��ȭ�� �޾ƿԴ�.

		String query2 = "insert into conversation_save_tbl values(auto_increment.nextval,?,?,'2019-09-24')";
		pstmt = conn.prepareStatement(query2);
		pstmt.setString(1, reply);
		pstmt.setInt(2, cr_id);

		pstmt.executeUpdate();

		String query3 = "select * from conversation_save_tbl";
		pstmt = conn.prepareStatement(query3);
		rs = pstmt.executeQuery();
		while (rs.next()) {
			int scs_id = rs.getInt(1);
			String sreply = rs.getString(2);
			int scr_id_fk = rs.getInt(3);
			String time = rs.getString(4);

			System.out.println(scs_id + " " + sreply + " " + scr_id_fk + " " + time);
		}

	}

}
