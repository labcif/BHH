package main.pt.ipleiria.estg.dei.labcif.bhh.database;

import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.utils.LoggerBHH;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DataWarehouseConnection {
    private static String fullPathConnection;
    private static final String DATABASE_FILENAME = "browser-history.db";
    private static Connection connection;
    private static DataWarehouseConnection instance;
    private static LoggerBHH<DataWarehouseConnection> loggerBHH = new LoggerBHH<>(DataWarehouseConnection.class);

    private DataWarehouseConnection() throws ConnectionException {
        try {
            connect();
        } catch (SQLException | ClassNotFoundException e) {
            instance = null;
            loggerBHH.error("Connection to database failed - Reason: " + e.getMessage());
            throw new ConnectionException("Connection to database failed - Reason: " + e.getMessage());
        }
    }
    private static void connect() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection(fullPathConnection);
    }

    public static Connection getConnection(String databaseDirectory) throws SQLException, ClassNotFoundException, ConnectionException {
        if (instance == null) {//TODO: allow to recreate a connection if databaseDirectory changed
            Path path = Paths.get(databaseDirectory, DATABASE_FILENAME);
            fullPathConnection = "jdbc:sqlite:" + path.toString();
            instance = new DataWarehouseConnection();
        }
        if (connection.isClosed()) {
            connect();
        }
        return connection;
    }


    public static void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
            instance = null;
            connection = null;
        } catch (SQLException e) {
            loggerBHH.warn("Connection couldn't be closed: " + e.getMessage());
        }
    }

    public static String getFullConnection() {
        if (instance == null) {
            throw new IllegalAccessError();
        }
        return fullPathConnection;
    }

    public static String getDatabaseFilename() {
        return DATABASE_FILENAME;
    }
}
