package main.pt.ipleiria.estg.dei.db;

import main.pt.ipleiria.estg.dei.model.BrowserEnum;
import main.pt.ipleiria.estg.dei.model.OperatingSystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


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
}
