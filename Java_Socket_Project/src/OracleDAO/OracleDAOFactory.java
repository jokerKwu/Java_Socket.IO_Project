package OracleDAO;

import java.sql.SQLException;

import DB.DAOFactory;

public class OracleDAOFactory extends DAOFactory {
	OracleDBConnection oconn = null;
	OracleDAO oracleDAO = null;

	public OracleDAOFactory() {
		oconn = new OracleDBConnection();
		oracleDAO = new OracleDAO();
	}

	public OracleDAOFactory(OracleDBConnection oconn, OracleDAO oracleDAO) {
		this.oconn = oconn;
		this.oracleDAO = oracleDAO;
	}
	
	@Override
	public void insertID(String id) {
		String query="insert into user_tbl(username) values("+id+");";
		try {
			oracleDAO.DAO_select_UserId(oconn.getConnection(), query);
			System.out.println("쿼리문 통과");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("문제 발생?");
			e.printStackTrace();
		}
	}

}
