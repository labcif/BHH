package main.pt.ipleiria.estg.dei.db.etl;

import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.utils.Logger;
import org.sleuthkit.autopsy.casemodule.Case;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DataWarehouseConnection {
    public static final String FULL_PATH_CONNECTION = "jdbc:sqlite:" + Case.getCurrentCase().getCaseDirectory() + "/browser-history.db";
    private static Connection connection;
    private static DataWarehouseConnection instance;
    private Logger<DataWarehouseConnection> logger = new Logger<>(DataWarehouseConnection.class);

    private DataWarehouseConnection() throws ConnectionException {
        try {
            connect();
        } catch (SQLException | ClassNotFoundException e) {
            instance = null;
            logger.error("Connection to database failed - Reason: " + e.getMessage());
            throw new ConnectionException("Connection to database failed - Reason: " + e.getMessage());
        }
    }
    private static void connect() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection(FULL_PATH_CONNECTION);
    }

    public static Connection getConnection() throws SQLException, ClassNotFoundException, ConnectionException {
        if (instance == null) {
            instance = new DataWarehouseConnection();
        }
        if (connection.isClosed()) {
            connect();
        }
        return connection;
    }

    public static DataWarehouseConnection getInstance() {
        return instance;
    }

    public void closeConnection() {
        try {
            connection.close();
            instance = null;
            connection = null;
        } catch (SQLException e) {
            logger.warn("Connection couldn't be closed: " + e.getMessage());
        }
    }
}
