  package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
	
	public Connection Get_Connection() throws Exception
	{
		
		try
		{

		String connectionURL = "jdbc:postgresql://localhost:5432/locationAnalyticsManager";
		Connection connection = null;
		Class.forName("org.postgresql.Driver").newInstance();
		connection = DriverManager.getConnection(connectionURL, "postgres", "password");


	    return connection;
		}
		catch (SQLException e)
		{
		throw e;	
		}
		catch (Exception e)
		{
		throw e;	
		}
	}

}
