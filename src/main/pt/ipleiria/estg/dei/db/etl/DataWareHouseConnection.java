package main.pt.ipleiria.estg.dei.db.etl;

import main.pt.ipleiria.estg.dei.db.ConnectionFactory;
import main.pt.ipleiria.estg.dei.exceptions.ExtractionException;

import java.sql.Connection;
import java.sql.SQLException;

public class DataWareHouseConnection {
    private static DataWareHouseConnection ourInstance = new DataWareHouseConnection();
    private static Connection datawarehouseConnection;

    private DataWareHouseConnection(){
        try {
            datawarehouseConnection = ConnectionFactory.getConnection();
        } catch (SQLException | ClassNotFoundException e) {
            throw new ExtractionException(e.getMessage());
        }
    }

    public static DataWareHouseConnection getOurInstance() {
        return ourInstance;
    }

    public static Connection getDatawarehouseConnection() {
        return datawarehouseConnection;
    }
}
