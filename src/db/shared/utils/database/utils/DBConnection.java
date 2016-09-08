package db.shared.utils.database.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {

    public static Connection con = null;

    public Connection createMySqlConnection() {

        if (con == null) {
            String url = "jdbc:mysql://10.101.15.48:3306/test";
            String username = "root";
            String password = "";

            try {
                Class.forName("com.mysql.jdbc.Driver");
                System.out.println("Connecting to the " + url + " database...");
                con = DriverManager.getConnection(url, username, password);
            } catch (Exception e) {
                System.out.println("Error occurs while creating database connection" + e.getMessage());
            }
        }
        return con;
    }

    public Connection createMSSqlConnection() {
        String db_connect_string = "jdbc:sqlserver://DEV-54;databaseName=FMSIClient;integratedSecurity=false";
        String db_userid = "FMSI_APP";
        String db_password = "00001111";
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(db_connect_string, db_userid, db_password);
            System.out.println(con + " Connection created");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occurs while creating connection to sql database" + e.getMessage());
        }
        return con;
    }

    public static void closeConnection() {
        try {
            if (con != null && !con.isClosed())
                con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}