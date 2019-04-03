package main.pt.ipleiria.estg.dei.model.browsers;

import main.pt.ipleiria.estg.dei.db.etl.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.exceptions.TransformationException;
import main.pt.ipleiria.estg.dei.utils.Logger;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.services.FileManager;
import org.sleuthkit.autopsy.datamodel.ContentUtils;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.TskCoreException;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Firefox extends Browser {
    private Logger<Firefox> logger = new Logger<>(Firefox.class);

    public Firefox(IngestJobContext context) {
        super(context);
    }

    @Override
    public void run(Content dataSource) {
        dataFound = false;
        this.dataSource = dataSource;
        runHistory();
    }

    @Override
    public void runHistory() {
        try {
            FileManager fileManager = Case.getCurrentCase().getServices().getFileManager();
            List<AbstractFile> history = fileManager.findFiles(dataSource,  "places.sqlite", "AppData/Roaming/Mozilla/Firefox/Profiles");

            history.forEach(file->{
                String userName = file.getParentPath().split("/")[2];

                //We have to copy this file to the temp directory
                String tempPath = getTempPath(Case.getCurrentCase(), getBrowserName()) + File.separator + "places" + userName + ".sqlite";
                try {
                    ContentUtils.writeToFile(file, new File(tempPath), context::dataSourceIngestIsCancelled);
                    runETLProcess(tempPath, userName);
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            });

        } catch (TskCoreException e) {
            throw new ExtractionException("this is to remove - " + e.getMessage());
        }
    }

    @Override
    public void extractAllTables() {
        extractPlaces();
    }

    private void extractPlaces() {
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
    public void transformAllTables(String user) {
        try {
            transformUrlTable(user);
        } catch (SQLException  e) {
            logger.error(e.getMessage());
            throw new TransformationException("Error transforming tables: " + e.getMessage());
        }
    }

    @Override
    public void deleteExtractTables() {
        try {
            Statement stmt = DataWarehouseConnection.getDatawarehouseConnection().createStatement();
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
        } catch (SQLException e) {
            logger.error("Error cleaning extracted tables - " + e.getMessage() + " From: " +getBrowserName());
            throw new ExtractionException("Error cleaning extracted tables - " + e.getMessage() + " From: " +getBrowserName());
        }
    }

    private void transformUrlTable(String user) throws SQLException {
        PreparedStatement preparedStatement = DataWarehouseConnection.getDatawarehouseConnection().prepareStatement(
                "INSERT INTO t_clean_url (url_full, url_domain, url_path, url_title, url_typed_count, " +
                                                "url_visit_time, url_user_origin, url_browser_origin ) " +
                        "SELECT  mp.url as url_full, " +
                                "replace( SUBSTR( substr(mp.url, instr(mp.url, '://')+3), 0,instr(substr(mp.url, instr(mp.url, '://')+3),'/')), 'www.', '') as url_domain, " +
                                "'TODO: path', title as url_title, typed as url_typed_count, " +
                                "strftime('%d-%m-%Y %H:%M:%S', datetime(mh.visit_date/1000000, 'unixepoch', 'localtime')) as url_visit_time, " +
                                "'" + user + "', '" + getBrowserName() + "' " +
                        "FROM t_ext_mozila_places mp, t_ext_mozila_historyvisits mh " +
                        "WHERE mp.id = mh.place_id " +
                        "and url_domain <> ''; ");

        preparedStatement.executeUpdate();
    }



    @Override
    public String getBrowserName() {
        return "Firefox";
    }
}
