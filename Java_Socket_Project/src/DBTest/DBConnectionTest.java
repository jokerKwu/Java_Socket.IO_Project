package DBTest;

import java.sql.SQLException;

import DB.DAOFactory;
import OracleDAO.OracleDAO;
import OracleDAO.OracleDBConnection;

public class DBConnectionTest {
	static DAOFactory d;
	public static void main(String[] args) {
		OracleDBConnection od=OracleDBConnection.getInstance();
		try {
			OracleDAO q=OracleDAO.getInstance();
			//q.DAO_select_UserId(od.getConnection(), "go");		//���� �α��� ������
			//q.DAO_insert_Reply(od.getConnection(), 1,2,"��ȭ�Դϴ�");	//��ȭ ������
			//q.DAO_select_lastReply(od.getConnection(), 1);	//�ش� ������ ������ ��ȭ ��������
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
