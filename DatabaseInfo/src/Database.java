import java.io.Console;
import java.sql.*;
import java.util.logging.ConsoleHandler;
import javax.sql.*;

public class Database
{
	private static final String HOST_URL = "forgiftandforget.chfppzvg2vo4.us-east-2.rds.amazonaws.com";
	private static final String PORT = "3306";
	private static final String DB_INSTANCE_NAME = "forgiftandforget";
	private static final String MASTER_USERNAME = "forgiftandforget";
	private static final String MASTER_PASSWORD = "forgiftandforget";
	private  static  final String CONN_STRING = String.format("jdbc:mysql://%s:%s/%s", HOST_URL, PORT, DB_INSTANCE_NAME);

	private Connection conn = null;

	Database()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(CONN_STRING, MASTER_USERNAME, MASTER_PASSWORD);
			System.out.println("Database connection established");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public int ExecSqlCmd(String cmd)
	{
		try
		{
			Statement sqlStatement = conn.createStatement();
			ResultSet resultSet = sqlStatement.executeQuery(cmd);
			while (resultSet.next())
			{
				String tmpStr = resultSet.getString("charCol");
				System.out.println(tmpStr);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}

		return 0;
	}
}