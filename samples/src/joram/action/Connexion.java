package action;

import java.sql.SQLException;

import java.util.Properties;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleDriver;

public class Connexion
{
	final private static String LOGIN = "ag092850";
    final private static String MDP = "ag092850";
    final private static String URL_FAC = "jdbc:oracle:thin:@butor:1521:ensb2017";
    final private static String URL_EXTERIEUR = "jdbc:oracle:thin:@ufrsciencestech.u-bourgogne.fr:25561:ensb2017";
	private static boolean co_fac = false;
	
	public static OracleConnection connect() throws SQLException
	{
		OracleDriver dr = new OracleDriver();
		Properties prop = new Properties();
		prop.setProperty("user", Connexion.LOGIN);
		prop.setProperty("password", Connexion.MDP);
		try
		{
			co_fac = true;
			return (OracleConnection)dr.connect(Connexion.URL_FAC, prop);
		}
		catch (SQLException ex) 
		{
			co_fac = false;
			return (OracleConnection)dr.connect(Connexion.URL_EXTERIEUR, prop);
		}
	}
	
	public static boolean isCoFac()
	{
		return co_fac;
	}
}