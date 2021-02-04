package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataHandler
{
	public static Connection getConnection()
	{
		Connection con = null;
		
		try
		{
			Class.forName("com.mysql.cj.jdbc.Driver");
			String usr = "";
			String pwd = "";
			String ip = "";
			String port = "3306";
			String databasename = "";
			String url = "jdbc:mysql://" + ip + ":" + port + "/" + databasename;
			
			con = DriverManager.getConnection(url, usr, pwd);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return con;
	}
	
	public static int getResultSize(String query) throws SQLException
	{
		ResultSet rs = getConnection().createStatement().executeQuery(query);
		int count = 0;
		
		while(rs.next())
			count++;
		
		return count;
	}
	
	public static String getQuoteByIndex(int index, String userID)
	{
		try
		{
			ResultSet rs = getConnection().createStatement().executeQuery("select quote from Wilbur_approvedquotes "
					+ "where userid = \"" + userID + "\" and userindex = " + index);
			
			rs.next();
			return rs.getString("quote");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
}
