package db.shared.utils.database.utils;

import java.sql.Connection;

public class DBUtilities {
    public static DBConnection dbConnection = new DBConnection();

    public DBUtilities() {
    }

    /**
     * Method to setup DB connection
     *
     * @return DB connection object
     */
    public static Connection setupMySqlDBConnection() {
        return dbConnection.createMySqlConnection();
    }

    public static Connection setupMSSqlDBConnection() {
        return dbConnection.createMSSqlConnection();
    }

}