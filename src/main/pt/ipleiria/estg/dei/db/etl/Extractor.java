package main.pt.ipleiria.estg.dei.db.etl;

import main.pt.ipleiria.estg.dei.db.ConnectionFactory;
import main.pt.ipleiria.estg.dei.events.EtlObserver;
import main.pt.ipleiria.estg.dei.events.Subject;
import main.pt.ipleiria.estg.dei.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.model.OperatingSystem;
import main.pt.ipleiria.estg.dei.utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static main.pt.ipleiria.estg.dei.model.BrowserEnum.CHROME;

public class Extractor {
    private static Extractor extractor;
    private Logger<Extractor> logger = new Logger<>(Extractor.class);
    private Subject subject;
    private EtlObserver etlObserver;

    protected Extractor() {
        subject = new Subject();
        etlObserver = new EtlObserver(subject);

        try {
            subject.setProcessFase("Extraction");

            cleanTExtTables();
            runFirstExtraction();

            subject.unregister(etlObserver);

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

        //Statement statement = fontConnection.createStatement();

        PreparedStatement preparedStatement =
                DataWareHouseConnection.getDatawarehouseConnection().prepareStatement( "ATTACH DATABASE '"+ OperatingSystem.getLocation(CHROME) +"' AS externalUrls");
        preparedStatement.executeUpdate();

     /*   preparedStatement =
                DataWareHouseConnection.getDatawarehouseConnection().prepareStatement( "ATTACH DATABASE '"+ OperatingSystem.getLocation(FIREFOX) +"' AS externalUrlsFireFox");
        preparedStatement.executeUpdate();*/

        extractTable("t_ext_chrome_urls", "urls");
        extractTable("t_ext_chrome_visits", "visits");
        extractTable("t_ext_chrome_visit_source", "visit_source");
        extractTable("t_ext_chrome_downloads", "downloads");
        extractTable("t_ext_chrome_downloads_slices", "downloads_slices");
        extractTable("t_ext_chrome_downloads_url_chains", "downloads_url_chains");
        extractTable("t_ext_chrome_keyword_search_terms", "keyword_search_terms");
        extractTable("t_ext_chrome_meta", "meta");
        extractTable("t_ext_chrome_segment_usage", "segment_usage");
        extractTable("t_ext_chrome_segments", "segments");
        extractTable("t_ext_chrome_sqlite_sequence", "sqlite_sequence");
        extractTable("t_ext_chrome_typed_url_sync_metadata", "typed_url_sync_metadata");

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

        fontConnection.close();
    }

    private void extractTable(String newTable, String oldTable) throws SQLException {
        DataWareHouseConnection.getDatawarehouseConnection()
                .prepareStatement("INSERT INTO main." + newTable + " SELECT * FROM externalUrls." + oldTable)
                .executeUpdate();
    }

    private void insertInTInfoExtract(String tablename) throws SQLException {

        PreparedStatement preparedStatement =
                DataWareHouseConnection.getDatawarehouseConnection().prepareStatement("INSERT INTO t_info_extract (name, last_extraction) VALUES (?, DateTime('now'));");
        preparedStatement.setString(1, tablename);
        preparedStatement.executeUpdate();
    }

    private void cleanTExtTables() throws SQLException {

        Statement stmt = DataWareHouseConnection.getDatawarehouseConnection().createStatement();
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
        stmt.execute("DELETE FROM t_info_extract;");//TODO: this is to remove. Only here to speed up debug
    }
}
