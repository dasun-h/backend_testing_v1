package db.shared.utils.database.utils;

import java.sql.Connection;

public class DBUtilities {

    public DBUtilities() {

    }

    /**
     * Method to setup DB connection
     *
     * @return DB connection object
     */
    public static Connection setupMySqlDBConnection() {
        DBConnection dbConnection = new DBConnection();
        return dbConnection.createMySqlConnection();
    }

/*    public static Connection setupSqlDBConnection() {
        //TODO
    }*/

}