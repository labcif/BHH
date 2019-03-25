package main.pt.ipleiria.estg.dei.db.etl;

import main.pt.ipleiria.estg.dei.db.ConnectionFactory;
import main.pt.ipleiria.estg.dei.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.model.BrowserEnum;
import main.pt.ipleiria.estg.dei.model.OperatingSystem;
import main.pt.ipleiria.estg.dei.utils.Logger;

import java.sql.*;

import static main.pt.ipleiria.estg.dei.model.BrowserEnum.CHROME;

public class Extractor {
    private static Extractor extractor;
    private Logger<Extractor> logger = new Logger<>(Extractor.class);

    protected Extractor() {
        try {
            cleanTExtTables();
            runFirstExtraction();
        } catch (SQLException | ClassNotFoundException e) {
            logger.error(e.getMessage());
            throw new ExtractionException(e.getMessage());
        }
    }

    public static void run() {
        if (extractor == null) {
            extractor = new Extractor();
        } else {
            //TODO: run next extractions
        }
    }

    private void runFirstExtraction() throws SQLException, ClassNotFoundException {
        extractDataGoogleChrome();
    }

    private void extractDataGoogleChrome() throws SQLException, ClassNotFoundException {
        Connection fontConnection = ConnectionFactory.getConnection(CHROME);

        Statement statement = fontConnection.createStatement();

        PreparedStatement preparedStatement =
                DataWareHouseConnection.getDatawarehouseConnection().prepareStatement( "ATTACH DATABASE '"+ OperatingSystem.getLocation(CHROME) +"' AS externalUrls");
        preparedStatement.executeUpdate();

        extractUrlsTable(statement);
        extractVisitsTable(statement);

        insertInTInfoExtract("t_ext_urls");
        insertInTInfoExtract("t_ext_visits");

        fontConnection.close();
    }

    //TODO: if this is way too slow. We have to find a way to copy directly the table from differents databases
    private void extractUrlsTable(Statement statement) throws SQLException {

        PreparedStatement preparedStatement = DataWareHouseConnection.getDatawarehouseConnection().prepareStatement("INSERT INTO externalUrls.t_ext_urls SELECT * FROM main.urls");
        preparedStatement.executeUpdate();

       /* ResultSet rs = statement.executeQuery("SELECT * FROM urls;");

        while (rs.next()) {
            PreparedStatement preparedStatement =
                    DataWareHouseConnection.getDatawarehouseConnection().prepareStatement("INSERT INTO t_ext_urls (url, title, visit_count, typed_count, last_visit_time, hidden) " +
                            " VALUES(?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, rs.getString("url"));
            preparedStatement.setString(2,  rs.getString("title"));
            preparedStatement.setLong(3, Long.parseLong(rs.getString("visit_count")));
            preparedStatement.setLong(4,Long.parseLong(rs.getString("typed_count")));
            preparedStatement.setLong(5, Long.parseLong(rs.getString("last_visit_time")));
            preparedStatement.setLong(6,  Long.parseLong(rs.getString("hidden")));
            preparedStatement.executeUpdate();
        }*/
    }
    private void extractVisitsTable(Statement statement) throws SQLException {

        PreparedStatement preparedStatement = DataWareHouseConnection.getDatawarehouseConnection().prepareStatement("INSERT INTO externalUrls.t_ext_visits  SELECT * FROM main.visits");
        preparedStatement.executeUpdate();
        /*
        ResultSet rs = statement.executeQuery("SELECT * FROM visits;");

        while (rs.next()) {
            PreparedStatement preparedStatement =
                    DataWareHouseConnection.getDatawarehouseConnection().prepareStatement("INSERT INTO t_ext_visits (url, visit_time, from_visit, transition, segment_id, visit_duration, incremented_omnibox_typed_score) " +
                            " VALUES(?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, rs.getString("url"));
            preparedStatement.setLong(2,  Long.parseLong(rs.getString("visit_time")));
            preparedStatement.setLong(3, Long.parseLong(rs.getString("from_visit")));
            preparedStatement.setLong(4,Long.parseLong(rs.getString("transition")));
            preparedStatement.setLong(5, Long.parseLong(rs.getString("segment_id")));
            preparedStatement.setLong(6,  Long.parseLong(rs.getString("visit_duration")));
            preparedStatement.setBoolean(7,  Boolean.getBoolean(rs.getString("incremented_omnibox_typed_score")));
            preparedStatement.executeUpdate();
        }*/
    }

    private void insertInTInfoExtract(String tablename) throws SQLException {
        PreparedStatement preparedStatement =
                DataWareHouseConnection.getDatawarehouseConnection().prepareStatement("INSERT INTO t_info_extract (name, last_extraction) VALUES (?, DateTime('now'));");
        preparedStatement.setString(1, tablename);
        preparedStatement.executeUpdate();
    }

    private void cleanTExtTables() throws SQLException {
        Statement stmt = DataWareHouseConnection.getDatawarehouseConnection().createStatement();
        stmt.execute("DELETE FROM t_ext_urls;");
        stmt.execute("DELETE FROM t_ext_visits;");
        stmt.execute("DELETE FROM t_info_extract;");//TODO: this is to remove. Only here to speed up debug
    }
}
