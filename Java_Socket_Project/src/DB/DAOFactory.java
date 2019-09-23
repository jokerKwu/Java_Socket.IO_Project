package DB;

import MySQLDAO.MySQLDAOFactory;
import OracleDAO.OracleDAOFactory;

public abstract class DAOFactory {
	public static final int MYSQL =0;
	public static final int ORACLE =1;

	
	public DAOFactory getFactory(int type) {
		switch(type) {
		case MYSQL:
			return new MySQLDAOFactory();
		case ORACLE:
			return new OracleDAOFactory();
		default:
			return null;
		}
	}
	public abstract void insertID(String id);
	

}
