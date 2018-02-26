package cs472.forgiftandforget.DatabaseClasses;

/**
 * Created by Tristan on 2/25/2018.
 */

import java.sql.*;

public class Database {
    private static final String HOST_URL = "faf.chfppzvg2vo4.us-east-2.rds.amazonaws.com";
    private static final String PORT = "3306";
    private static final String DB_INSTANCE_NAME = "faf";
    private static final String MASTER_USERNAME = "sa";
    private static final String MASTER_PASSWORD = "password123";
    private static final String CONN_STRING = String.format("jdbc:mysql://%s:%s/%s", HOST_URL, PORT, DB_INSTANCE_NAME);

    private static final int QUERY_TIMEOUT_SECONDS = 15;

    protected static Connection conn = null;
    protected static ResultSet lastQueryResult = null;
    protected static boolean lastQuerySuccess = false;
    protected static String lastErrorMessage = "";

    public static void ExecSqlCmd(String cmd) {
        GetOrRefreshConnection();

        try {
            Statement sqlStatement = conn.createStatement();
            sqlStatement.execute(cmd);

            lastQueryResult = sqlStatement.getResultSet();
            lastQuerySuccess = true;
            lastErrorMessage = "";
        } catch (Exception e) {
            e.printStackTrace();
            lastQueryResult = null;
            lastQuerySuccess = false;
            lastErrorMessage = cmd + " CAUSED: \n" + e.toString();
        }
    }

    protected static void GetOrRefreshConnection() {
        try {
            if (conn == null || !conn.isValid(QUERY_TIMEOUT_SECONDS)) {
                Class.forName("com.mysql.jdbc.Driver"); //seaches for the external mysql-connector library
                conn = DriverManager.getConnection(CONN_STRING, MASTER_USERNAME, MASTER_PASSWORD);
                System.out.println("Database connection established");
            }
        } catch (Exception e) {
            e.printStackTrace();
            conn = null;
        }
    }

}
