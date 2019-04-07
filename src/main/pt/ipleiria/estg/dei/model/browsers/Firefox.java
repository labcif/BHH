package main.pt.ipleiria.estg.dei.model.browsers;

import main.pt.ipleiria.estg.dei.db.etl.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.exceptions.TransformationException;
import main.pt.ipleiria.estg.dei.utils.Logger;
import org.sleuthkit.autopsy.ingest.IngestJobContext;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class Firefox extends Browser {
    private Logger<Firefox> logger = new Logger<>(Firefox.class);

    public Firefox(IngestJobContext context) {
        super(context);
    }

    @Override
    public void extractAllTables() throws ConnectionException {
        extractPlaces();
    }

    private void extractPlaces() throws ConnectionException {
        extractTable("t_ext_mozila_anno_attributes", "moz_anno_attributes", EXTERNAL_URL);
        extractTable("t_ext_mozila_annos", "moz_annos", EXTERNAL_URL);
        extractTable("t_ext_mozila_bookmarks", "moz_bookmarks", EXTERNAL_URL);
        extractTable("t_ext_mozila_bookmarks_deleted", "moz_bookmarks_deleted", EXTERNAL_URL);
        extractTable("t_ext_mozila_historyvisits", "moz_historyvisits", EXTERNAL_URL);
        extractTable("t_ext_mozila_hosts", "moz_hosts", EXTERNAL_URL);
        extractTable("t_ext_mozila_inputhistory", "moz_inputhistory", EXTERNAL_URL);
        extractTable("t_ext_mozila_items_annos", "moz_items_annos", EXTERNAL_URL);
        extractTable("t_ext_mozila_keywords", "moz_keywords", EXTERNAL_URL);
        extractTable("t_ext_mozila_meta", "moz_meta", EXTERNAL_URL);
        extractTable("t_ext_mozila_origins", "moz_origins", EXTERNAL_URL);
        extractTable("t_ext_mozila_places", "moz_places", EXTERNAL_URL);
        extractTable("t_ext_mozila_sqlite_sequence", "moz_sqlite_sequence", EXTERNAL_URL);
        extractTable("t_ext_mozila_sqlite_stat1", "moz_sqlite_stat1", EXTERNAL_URL);
    }

    @Override
    public void transformAllTables(String user) throws ConnectionException {
        transformUrlTable(user);
    }

    @Override
    public void deleteExtractTables() throws ConnectionException {
        try {
            Statement stmt = DataWarehouseConnection.getConnection().createStatement();
            stmt.execute("DELETE FROM t_ext_mozila_anno_attributes;");
            stmt.execute("DELETE FROM t_ext_mozila_annos;");
            stmt.execute("DELETE FROM t_ext_mozila_bookmarks;");
            stmt.execute("DELETE FROM t_ext_mozila_bookmarks_deleted;");
            stmt.execute("DELETE FROM t_ext_mozila_historyvisits;");
            stmt.execute("DELETE FROM t_ext_mozila_hosts;");
            stmt.execute("DELETE FROM t_ext_mozila_inputhistory;");
            stmt.execute("DELETE FROM t_ext_mozila_items_annos;");
            stmt.execute("DELETE FROM t_ext_mozila_keywords;");
            stmt.execute("DELETE FROM t_ext_mozila_meta;");
            stmt.execute("DELETE FROM t_ext_mozila_origins;");
            stmt.execute("DELETE FROM t_ext_mozila_places;");
            stmt.execute("DELETE FROM t_ext_mozila_sqlite_sequence;");
            stmt.execute("DELETE FROM t_ext_mozila_sqlite_stat1;");
        } catch (SQLException | ClassNotFoundException e) {
            throw new ExtractionException(getModuleName(), "t_ext_","Error cleaning extracted tables - " + e.getMessage());
        }
    }

    private void transformUrlTable(String user) throws ConnectionException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = DataWarehouseConnection.getConnection().prepareStatement(
                    "INSERT INTO t_clean_url (url_full, url_domain, url_path, url_title, url_typed_count, " +
                                                    "url_visit_time, url_user_origin, url_browser_origin ) " +
                            "SELECT  mp.url as url_full, " +
                                    "replace( SUBSTR( substr(mp.url, instr(mp.url, '://')+3), 0,instr(substr(mp.url, instr(mp.url, '://')+3),'/')), 'www.', '') as url_domain, " +
                                    "'TODO: path', title as url_title, typed as url_typed_count, " +
                                    "strftime('%d-%m-%Y %H:%M:%S', datetime(mh.visit_date/1000000, 'unixepoch', 'localtime')) as url_visit_time, " +
                                    "'" + user + "', '" + getModuleName() + "' " +
                            "FROM t_ext_mozila_places mp, t_ext_mozila_historyvisits mh " +
                            "WHERE mp.id = mh.place_id " +
                            "and url_domain <> ''; ");
            preparedStatement.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            throw new TransformationException(getModuleName(), "t_clean_url","Error transforming tables: " + e.getMessage());
        }
    }


    @Override
    public String getModuleName() {
        return "Firefox";
    }

    @Override
    public String getPathToBrowserHistory() {
        return "AppData/Roaming/Mozilla/Firefox/Profiles";
    }

    @Override
    public String getHistoryFilename() {
        return "places.sqlite";
    }
}
