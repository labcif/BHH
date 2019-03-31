package main.pt.ipleiria.estg.dei.model.browsers;

import main.pt.ipleiria.estg.dei.db.etl.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.exceptions.TransformationException;
import main.pt.ipleiria.estg.dei.utils.Logger;
import org.sleuthkit.autopsy.casemodule.Case;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Data  implements ETLProcess{
    private Logger<Data> logger = new Logger<>(Data.class);
    private Connection connection;
    protected final static String EXTERNAL_URL = "externalUrls";

    public void runETLProcess(String path, String user) {
        runExtraction(path);
        runTransformation(user);
    }

    public void runExtraction(String path) {
        try {
            deleteExtractTables();
            startTransaction(path);
            extractAllTables();
            endTransaction();

            insertAllRowsInTInfoExtract();

        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Couldn't connect to db. Path: " + path + " - Error: " + e.getMessage());
            throw new TransformationException("Couldn't connect to db. Path: " + path + " - Error: " + e.getMessage());
        }
    }

    @Override
    public void runTransformation(String user) {
        deletedCleanTables();
        transformAllTables(user);
    }

    public void startTransaction(String path) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        DataWarehouseConnection.getDatawarehouseConnection()
                .prepareStatement( "ATTACH DATABASE '"+ path +"' AS " +EXTERNAL_URL)
                .executeUpdate();
        DataWarehouseConnection.getDatawarehouseConnection().setAutoCommit(false);
    }

    public void endTransaction() throws SQLException {
        DataWarehouseConnection.getDatawarehouseConnection().commit();
        DataWarehouseConnection.getDatawarehouseConnection()
                .prepareStatement("DETACH DATABASE 'externalUrls'")
                .executeUpdate();
        connection.close();
    }

    public void extractTable(String newTable, String oldTable, String externalDB){
        try {
            //reason???? --- stupids versions have differrent columns
            //get columns of newTable
            Connection datawarehouseConnection = DataWarehouseConnection.getDatawarehouseConnection();
            Statement statement = datawarehouseConnection.createStatement();
            ResultSet rs = statement.executeQuery("PRAGMA table_info (" + newTable + ");");
            List<String> columnsHeaderNewTable = new ArrayList<>();
            while (rs.next()) {
                columnsHeaderNewTable.add(rs.getString("name"));
            }

            
            //Get columns of old table
            rs = connection.createStatement().executeQuery("PRAGMA table_info (" + oldTable + ");");
            List<String> columnsHeaderOldTable = new ArrayList<>();
            while (rs.next()) {
                columnsHeaderOldTable.add(rs.getString("name"));
            }

            //Get the ones that match
            columnsHeaderNewTable.retainAll(columnsHeaderOldTable);
            if (columnsHeaderNewTable.isEmpty()) {
                return;//Will have to notify that to the user
            }

            String columns = String.join( ", ", columnsHeaderNewTable);
            //make insertion with match
            DataWarehouseConnection.getDatawarehouseConnection()
                    .prepareStatement("INSERT INTO main." + newTable + " (" + columns + ") " +
                            "SELECT " + columns + " FROM " + externalDB + "." + oldTable)
                    .executeUpdate();
        } catch (SQLException e) {
            logger.warn("One table could't be extracted: " + oldTable + " - Reason: " + e.getMessage());
            throw new ExtractionException("One table could't be extracted: " + oldTable + " - Reason: " + e.getMessage());

        }
    }

    protected void insertInTInfoExtract(String tablename){
        try {
            PreparedStatement preparedStatement = DataWarehouseConnection.getDatawarehouseConnection()
                    .prepareStatement("INSERT INTO t_info_extract (name, last_extraction) VALUES (?, DateTime('now'));");
            preparedStatement.setString(1, tablename);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error inserting in t_info_extarct - reason: " +e.getSQLState());
            throw new ExtractionException("Error inserting in t_info_extarct - reason: " +e.getSQLState());

        }
    }

    protected static String getTempPath(Case aCase, String filename) {
        String tmpDir = aCase.getTempDirectory() + File.separator + "RecentActivity" + File.separator + filename;
        File dir = new File(tmpDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return tmpDir;
    }
}
