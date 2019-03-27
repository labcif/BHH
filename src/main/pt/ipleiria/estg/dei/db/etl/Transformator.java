package main.pt.ipleiria.estg.dei.db.etl;

import main.pt.ipleiria.estg.dei.exceptions.TransformationException;
import main.pt.ipleiria.estg.dei.utils.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class Transformator {

    private Logger<Transformator> logger = new Logger<>(Transformator.class);

    private Transformator() {
        try {
            cleanTables();
            transformUrlTable();
            transformDownloadsTable();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new TransformationException("Could't connect to database. Reason: " + e.getMessage());
        }

    }

    private void transformUrlTable() throws SQLException {
            PreparedStatement preparedStatement = DataWarehouseConnection.getDatawarehouseConnection().prepareStatement(
                " INSERT INTO t_clean_url (url_full, url_domain, url_path, url_title, url_visit_count, url_typed_count ) " +
                "SELECT  teu.url as url_full, replace( SUBSTR( substr(teu.url,instr(teu.url, '://')+3), 0,instr(substr(teu.url,instr(teu.url, '://')+3),'/')), 'www.', '') as url_domain, 'TODO: path',title as url_title, visit_count as url_visit_count, typed_count as url_typed_count" +
                " FROM t_ext_chrome_urls teu, t_ext_chrome_visits tev " +
                "WHERE teu.id = tev.url " +
                 "and url_domain <> '' ");

            preparedStatement.executeUpdate();
    }

    private void transformDownloadsTable() throws SQLException {
        PreparedStatement preparedStatement = DataWarehouseConnection.getDatawarehouseConnection().prepareStatement(
                " INSERT INTO t_clean_downloads (url_domain, url_full, type, danger_type, tab_url, download_target_path, beginning_date, ending_date, received_bytes, total_bytes, interrupt_reason) " +
                " SELECT  replace( SUBSTR( substr(site_url,instr(site_url, '://')+3), 0,instr(substr(site_url,instr(site_url, '://')+3),'/')), 'www.', '') as url_domain, referrer as url_full," +
                "        mime_type as type, danger_type, tab_url ,target_path as download_traget_path," +
                "        start_time as begining_date, end_time as end_date , received_bytes , total_bytes, interrupt_reason " +
                " FROM t_ext_chrome_downloads ");

        preparedStatement.executeUpdate();

    }

    private void cleanTables() throws SQLException {
        Statement stmt = DataWarehouseConnection.getDatawarehouseConnection().createStatement();
        stmt.execute("DELETE FROM t_clean_url;");
        stmt.execute("DELETE FROM t_clean_downloads;");
    }

    public static void tranform() {
        new Transformator();
    }


}
