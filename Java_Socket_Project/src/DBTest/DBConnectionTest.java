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
			System.out.println(q.DAO_select_UserId(od.getConnection(), "go"));		//유저 로그인 했을때
			//q.DAO_insert_Reply(od.getConnection(), 1,2,"대화입니다");	//대화 했을떄
			//q.DAO_select_lastReply(od.getConnection(), 10);	//해당 유저에 마지막 대화 가져오기
			//q.DAO_insert_saveConversation(od.getConnection(), 1); //해당 유저에 마지막 대화 저장하기.
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
