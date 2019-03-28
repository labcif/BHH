package main.pt.ipleiria.estg.dei.db.etl;

import main.pt.ipleiria.estg.dei.blocked.ISPLockedWebsites;
import main.pt.ipleiria.estg.dei.db.ConnectionFactory;
import main.pt.ipleiria.estg.dei.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.model.OperatingSystem;
import main.pt.ipleiria.estg.dei.utils.Logger;
import org.json.JSONException;

import java.io.IOException;
import java.sql.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static main.pt.ipleiria.estg.dei.model.BrowserEnum.CHROME;

public class Extractor {
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
        new Extractor();
    }

    private void runFirstExtraction() throws SQLException, ClassNotFoundException {
        extractDataGoogleChrome();
    }

    private void extractDataGoogleChrome() throws SQLException, ClassNotFoundException{
        Connection fontConnection = ConnectionFactory.getConnection(CHROME);
        DataWarehouseConnection.getDatawarehouseConnection()
                .prepareStatement( "ATTACH DATABASE '"+ OperatingSystem.getLocation(CHROME) +"' AS externalUrls")
                .executeUpdate();

        DataWarehouseConnection.getDatawarehouseConnection()
                .prepareStatement("ATTACH DATABASE '"+ OperatingSystem.getLocationEmail(CHROME) +"' AS externalLogins")
                .executeUpdate();

        DataWarehouseConnection.getDatawarehouseConnection().setAutoCommit(false);

        extractAllTables();
        extractBlokedSites();
        insertAllRowsInTInfoExtract();

        DataWarehouseConnection.getDatawarehouseConnection().commit();

        fontConnection.close();
    }

    private void insertAllRowsInTInfoExtract() throws SQLException {
        insertInTInfoExtract("t_ext_chrome_urls");
        insertInTInfoExtract("t_ext_chrome_visits");
        insertInTInfoExtract("t_ext_chrome_visit_source");
        insertInTInfoExtract("t_ext_chrome_downloads");
        insertInTInfoExtract("t_ext_chrome_downloads_slices");
        insertInTInfoExtract("t_ext_chrome_downloads_url_chains");
        insertInTInfoExtract("t_ext_chrome_keyword_search_terms");
        insertInTInfoExtract("t_ext_chrome_meta");
        insertInTInfoExtract("t_ext_chrome_segment_usage");
        insertInTInfoExtract("t_ext_chrome_segments");
        insertInTInfoExtract("t_ext_chrome_sqlite_sequence");
        insertInTInfoExtract("t_ext_chrome_typed_url_sync_metadata");
        insertInTInfoExtract("t_ext_blocked_websites");
    }

    private void extractAllTables() throws SQLException {
        extractTable("t_ext_chrome_urls", "urls", "externalUrls");
        extractTable("t_ext_chrome_visits", "visits", "externalUrls");
        extractTable("t_ext_chrome_visit_source", "visit_source", "externalUrls");
        extractTable("t_ext_chrome_downloads", "downloads", "externalUrls");
        extractTable("t_ext_chrome_downloads_slices", "downloads_slices", "externalUrls");
        extractTable("t_ext_chrome_downloads_url_chains", "downloads_url_chains", "externalUrls");
        extractTable("t_ext_chrome_keyword_search_terms", "keyword_search_terms", "externalUrls");
        extractTable("t_ext_chrome_meta", "meta", "externalUrls");
        extractTable("t_ext_chrome_segment_usage", "segment_usage", "externalUrls");
        extractTable("t_ext_chrome_segments", "segments", "externalUrls");
        extractTable("t_ext_chrome_sqlite_sequence", "sqlite_sequence", "externalUrls");
        extractTable("t_ext_chrome_typed_url_sync_metadata", "typed_url_sync_metadata", "externalUrls");
        extractTable("t_ext_chrome_login_data", "logins", "externalLogins");
    }

    private void extractTable(String newTable, String oldTable, String externalDB) throws SQLException {
        DataWarehouseConnection.getDatawarehouseConnection()
                .prepareStatement("INSERT INTO main." + newTable + " SELECT * FROM " + externalDB + "." + oldTable)
                .executeUpdate();
    }

    private void extractBlokedSites() throws SQLException {


        Set<String> urlSet = ISPLockedWebsites.INSTANCE.readJsonFromUrl("https://tofran.github.io/PortugalWebBlocking/blockList.json").keySet();

        PreparedStatement preparedStatement = DataWarehouseConnection.getDatawarehouseConnection().prepareStatement("INSERT INTO main.t_ext_blocked_websites (domain) " +
                " VALUES (?)");

        for (String url : urlSet) {
            preparedStatement.setString(1, url);
            preparedStatement.addBatch();
        }

        preparedStatement.executeBatch();
    }

    private void insertInTInfoExtract(String tablename) throws SQLException {

        PreparedStatement preparedStatement =
                DataWarehouseConnection.getDatawarehouseConnection()
                        .prepareStatement("INSERT INTO t_info_extract (name, last_extraction) VALUES (?, DateTime('now'));");
        preparedStatement.setString(1, tablename);
        preparedStatement.executeUpdate();
    }

    private void cleanTExtTables() throws SQLException {
        Statement stmt = DataWarehouseConnection.getDatawarehouseConnection().createStatement();
        stmt.execute("DELETE FROM t_ext_chrome_urls;");
        stmt.execute("DELETE FROM t_ext_chrome_visits;");
        stmt.execute("DELETE FROM t_ext_chrome_visit_source;");
        stmt.execute("DELETE FROM t_ext_chrome_downloads;");
        stmt.execute("DELETE FROM t_ext_chrome_downloads_slices;");
        stmt.execute("DELETE FROM t_ext_chrome_downloads_url_chains;");
        stmt.execute("DELETE FROM t_ext_chrome_keyword_search_terms;");
        stmt.execute("DELETE FROM t_ext_chrome_meta;");
        stmt.execute("DELETE FROM t_ext_chrome_segment_usage;");
        stmt.execute("DELETE FROM t_ext_chrome_segments;");
        stmt.execute("DELETE FROM t_ext_chrome_sqlite_sequence;");
        stmt.execute("DELETE FROM t_ext_chrome_typed_url_sync_metadata;");
        stmt.execute("DELETE FROM t_ext_blocked_websites;");
        stmt.execute("DELETE FROM t_info_extract;");//TODO: this is to remove. Only here to speed up debug
    }
}
