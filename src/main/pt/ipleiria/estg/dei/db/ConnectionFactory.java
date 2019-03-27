package main.pt.ipleiria.estg.dei.db;

import main.pt.ipleiria.estg.dei.model.BrowserEnum;
import main.pt.ipleiria.estg.dei.model.OperatingSystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static main.pt.ipleiria.estg.dei.db.etl.DatabaseCreator.FULL_PATH_CONNECTION;


public class ConnectionFactory {
    /**
     * Get a connection to database
     * @param
     * @return Connection object
     */
    public static Connection getConnection(BrowserEnum browser) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:" + OperatingSystem.getLocation(browser));
    }

    /**
     * Get a connection to our database that is used as datawarehouse
     * @return Connection object
     */
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        //TODO: This is not correct. If we don't run ingest module first, we'll get an error. since we dont have the path.
        //TODO: to solve this issue we must get the path from a Autopsy.
        return DriverManager.getConnection(FULL_PATH_CONNECTION);
    }
}
