package main.pt.ipleiria.estg.dei.labcif.bhh.modules;

import main.pt.ipleiria.estg.dei.labcif.bhh.database.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.labcif.bhh.events.IngestModuleProgress;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.TransformationException;
import main.pt.ipleiria.estg.dei.labcif.bhh.models.OperatingSystem;
import main.pt.ipleiria.estg.dei.labcif.bhh.utils.Utils;
import org.sleuthkit.autopsy.casemodule.Case;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Module implements ETLProcess {
    protected final static String FULL_DATE_FORMAT = "%Y-%m-%dT%H:%M:%S";
    protected final static String DATE_FORMAT = "%Y-%m-%d";
    protected final static String TIME_FORMAT = "%H:%M:%S";
    private Connection connection;
    protected final static String EXTERNAL_URL = "externalUrls";
    protected String databaseDirectory;

    public Module(String databaseDirectory) {
        this.databaseDirectory = databaseDirectory;
    }

    public void runETLProcess(String databaseOriginFullPath, String user, String profileName, String fullLocationFile, OperatingSystem os) throws ConnectionException {
        IngestModuleProgress.getInstance().incrementProgress(user, getModuleName(),"extraction");
        runExtraction(databaseOriginFullPath);
        IngestModuleProgress.getInstance().incrementProgress(user, getModuleName(),"tranformation");
        runTransformation(user, profileName, fullLocationFile, os);
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
    public void runTransformation(String user, String profileName, String fullLocationFile, OperatingSystem os) throws ConnectionException {
//        deleteCleanTables();
        transformAllTables(user, profileName, fullLocationFile, os);
    }

    public void startTransaction(String path) throws SQLException, ClassNotFoundException, ConnectionException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        DataWarehouseConnection.getConnection(databaseDirectory)
                .prepareStatement( "ATTACH DATABASE '"+ path +"' AS " +EXTERNAL_URL)
                .executeUpdate();
        DataWarehouseConnection.getConnection(databaseDirectory).setAutoCommit(false);
    }

    public void endTransaction() throws SQLException, ClassNotFoundException, ConnectionException {
        DataWarehouseConnection.getConnection(databaseDirectory).commit();
        DataWarehouseConnection.getConnection(databaseDirectory)
                .prepareStatement("DETACH DATABASE '"+ EXTERNAL_URL+"'")
                .executeUpdate();
        connection.close();
        DataWarehouseConnection.getConnection(databaseDirectory).setAutoCommit(true);
    }

    public void extractTable(String newTable, String oldTable, String externalDB) throws ConnectionException {
        try {
            //we need to check which columns are equals, because different versions of same browser change columns
            //get columns of newTable
            Connection datawarehouseConnection = DataWarehouseConnection.getConnection(databaseDirectory);
            Statement statement = datawarehouseConnection.createStatement();
            ResultSet rs = statement.executeQuery("PRAGMA table_info (" + newTable + ");");
            List<String> columnsHeaderNewTable = new ArrayList<>();
            while (rs.next()) {
                columnsHeaderNewTable.add(rs.getString("name"));
            }

            if (connection == null) {
                throw new ExtractionException(getModuleName(), newTable, "Connection to old table couldn't be established");
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
            DataWarehouseConnection.getConnection(databaseDirectory)
                    .prepareStatement("INSERT INTO main." + newTable + " (" + columns + ") " +
                            "SELECT " + columns + " FROM " + externalDB + "." + oldTable)
                    .executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            throw new ExtractionException(getModuleName(), oldTable, "One table could't be extracted: " + e.getMessage());
        }
    }


    protected static String getTempPath(Case aCase, String filename) {
        String tmpDir = aCase.getTempDirectory() + File.separator + "BrowserHistory" + File.separator + filename;
        return Utils.createDirectoryIfNotExists(tmpDir);
    }

    @Override
    public void deleteCleanTables() throws ConnectionException {
        try {
            Statement stmt = DataWarehouseConnection.getConnection(databaseDirectory).createStatement();
            stmt.execute("DELETE FROM t_clean_url;");
            stmt.execute("DELETE FROM t_clean_downloads;");
            stmt.execute("DELETE FROM t_clean_special_websites;");
            stmt.execute("DELETE FROM t_clean_logins;");
            stmt.execute("DELETE FROM t_clean_search_in_engines;");
        } catch (SQLException | ClassNotFoundException e) {
             throw new TransformationException(getModuleName(), "t_clean_","Error deleting clean tables - Reason:" + e.getMessage());
        }
    }
    public abstract String getModuleName();
}
