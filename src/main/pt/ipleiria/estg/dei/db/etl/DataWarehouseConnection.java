package main.pt.ipleiria.estg.dei.db.etl;

import main.pt.ipleiria.estg.dei.db.ConnectionFactory;
import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;

import java.sql.Connection;
import java.sql.SQLException;

public class DataWarehouseConnection {
    private static DataWarehouseConnection instance = new DataWarehouseConnection();
    private static Connection datawarehouseConnection;

    private DataWarehouseConnection(){
        try {
            datawarehouseConnection = ConnectionFactory.getConnection();
        } catch (SQLException | ClassNotFoundException e) {
            throw new ConnectionException(e.getMessage());
        }
    }

    public static DataWarehouseConnection getInstance() {
        return instance;
    }

    public static Connection getDatawarehouseConnection() {
        return datawarehouseConnection;
    }
}
