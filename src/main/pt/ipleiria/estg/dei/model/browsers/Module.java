package main.pt.ipleiria.estg.dei.model.browsers;

import main.pt.ipleiria.estg.dei.db.etl.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.events.IngestModuleProgress;
import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.exceptions.TransformationException;
import org.sleuthkit.autopsy.casemodule.Case;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Module implements ETLProcess{
    private Connection connection;
    protected final static String EXTERNAL_URL = "externalUrls";

    public void runETLProcess(String path, String user) throws ConnectionException {
        IngestModuleProgress.getInstance().incrementProgress(user, getModuleName(),"extraction");
        runExtraction(path);
        IngestModuleProgress.getInstance().incrementProgress(user, getModuleName(),"tranformation");
        runTransformation(user);
    }

    public void runExtraction(String path) throws ConnectionException {
        try {
            deleteExtractTables();
            startTransaction(path);
            extractAllTables();
        } catch (SQLException | ClassNotFoundException e) {
            throw new ExtractionException(getModuleName(), path, "Couldn't connect to db " + e.getMessage());
        }finally {
            try {
                endTransaction();
            } catch (SQLException | ClassNotFoundException e) {
            }
        }
    }

    @Override
    public void runTransformation(String user) throws ConnectionException {
//        deleteCleanTables();
        transformAllTables(user);
    }

    public void startTransaction(String path) throws SQLException, ClassNotFoundException, ConnectionException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        DataWarehouseConnection.getConnection()
                .prepareStatement( "ATTACH DATABASE '"+ path +"' AS " +EXTERNAL_URL)
                .executeUpdate();
        DataWarehouseConnection.getConnection().setAutoCommit(false);
    }

    public void endTransaction() throws SQLException, ClassNotFoundException, ConnectionException {
        DataWarehouseConnection.getConnection().commit();
        DataWarehouseConnection.getConnection()
                .prepareStatement("DETACH DATABASE '"+ EXTERNAL_URL+"'")
                .executeUpdate();
        connection.close();
        DataWarehouseConnection.getConnection().setAutoCommit(true);
    }

    public void extractTable(String newTable, String oldTable, String externalDB) throws ConnectionException {
        try {
            //we need to check which columns are equals, because different versions of same browser change columns
            //get columns of newTable
            Connection datawarehouseConnection = DataWarehouseConnection.getConnection();
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
            DataWarehouseConnection.getConnection()
                    .prepareStatement("INSERT INTO main." + newTable + " (" + columns + ") " +
                            "SELECT " + columns + " FROM " + externalDB + "." + oldTable)
                    .executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            throw new ExtractionException(getModuleName(), oldTable, "One table could't be extracted: " + e.getMessage());
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

    @Override
    public void deleteCleanTables() throws ConnectionException {
        try {
            Statement stmt = DataWarehouseConnection.getConnection().createStatement();
            stmt.execute("DELETE FROM t_clean_url;");
            stmt.execute("DELETE FROM t_clean_downloads;");
            stmt.execute("DELETE FROM t_clean_blocked_websites;");
            stmt.execute("DELETE FROM t_clean_emails;");
            stmt.execute("DELETE FROM t_clean_words;");
        } catch (SQLException | ClassNotFoundException e) {
             throw new TransformationException(getModuleName(), "t_clean_","Error deleting clean tables - Reason:" + e.getMessage());
        }
    }
    public abstract String getModuleName();
}
