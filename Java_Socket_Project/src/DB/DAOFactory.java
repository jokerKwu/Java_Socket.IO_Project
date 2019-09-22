package DB;

public class DAOFactory {
	public static final int MYSQL =0;
	public static final int ORACLE =1;
	
	public static DAOFactory getFactory(int type) {
		switch(type) {
		case MYSQL:
			return new MySQLDAOFactory();
		case ORACLE:
			return new OracleDAOFactory();
		default:
			return null;
		}
	}
}
