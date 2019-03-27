package main.pt.ipleiria.estg.dei.db.etl;

import main.pt.ipleiria.estg.dei.db.ConnectionFactory;
import main.pt.ipleiria.estg.dei.exceptions.TransformationException;
import main.pt.ipleiria.estg.dei.utils.Logger;

import java.sql.*;

public class Transformator {

    private Logger<Transformator> logger = new Logger<>(Transformator.class);

    private Transformator() {
        try {
            cleanTables();
            transformUrlTable();
            transformDownloadsTable();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new TransformationException(e.getMessage());
        }

    }

    private void transformUrlTable() throws SQLException {
        /*Statement statement = datawarehouseConnection.createStatement();

        // como já se falou o while tem um custo estupidamente alto)
        // not shure, mas provavelmente de forma a optimizar a velocidade pode-se fazer isto tudo em sql, sendo apenas necessário usar o while caso operações tipo decode sejam realizadas
        ResultSet rs = statement.executeQuery("SELECT  teu.url as url_full, replace( SUBSTR( substr(teu.url,instr(teu.url, '://')+3), 0,instr(substr(teu.url,instr(teu.url, '://')+3),'/')), 'www.', '') as url_domain, title, visit_count, typed_count" +
                                                " FROM t_ext_chrome_urls teu, t_ext_chrome_visits tev " +
                                                "WHERE teu.id = tev.url ");*/
/*
        while (rs.next()) {
            PreparedStatement preparedStatement =
                    datawarehouseConnection.prepareStatement(
                            "INSERT INTO t_clean_url (url_full, url_domain, url_path, url_title, url_visit_count, url_typed_count )" +
                            " VALUES(?, ?, ?, ?, ?, ?)");//TODO: falta acrescentar os restantes
            preparedStatement.setString(1, rs.getString("url_full"));
            preparedStatement.setString(2,  rs.getString("url_domain"));
            preparedStatement.setString(3, "TODO: path");
            preparedStatement.setString(4,rs.getString("title"));
            preparedStatement.setInt(5, rs.getInt("visit_count"));
            preparedStatement.setInt(6,  rs.getInt("typed_count"));
         }
*/
            PreparedStatement preparedStatement = DataWareHouseConnection.getDatawarehouseConnection().prepareStatement("" +
                " INSERT INTO t_clean_url (url_full, url_domain, url_path, url_title, url_visit_count, url_typed_count ) " +
                "SELECT  teu.url as url_full, replace( SUBSTR( substr(teu.url,instr(teu.url, '://')+3), 0,instr(substr(teu.url,instr(teu.url, '://')+3),'/')), 'www.', '') as url_domain, 'TODO: path',title as url_title, visit_count as url_visit_count, typed_count as url_typed_count" +
                " FROM t_ext_chrome_urls teu, t_ext_chrome_visits tev " +
                "WHERE teu.id = tev.url ");

            preparedStatement.executeUpdate();
    }

    private void transformDownloadsTable() throws SQLException {
        PreparedStatement preparedStatement = DataWareHouseConnection.getDatawarehouseConnection().prepareStatement("" +
                " INSERT INTO t_clean_downloads (url_domain, url_full, type, danger_type, tab_url, download_target_path, beginning_date, ending_date, received_bytes, total_bytes, interrupt_reason) " +
                " SELECT  replace( SUBSTR( substr(site_url,instr(site_url, '://')+3), 0,instr(substr(site_url,instr(site_url, '://')+3),'/')), 'www.', '') as url_domain, referrer as url_full," +
                "        mime_type as type, danger_type, tab_url ,target_path as download_traget_path," +
                "        start_time as begining_date, end_time as end_date , received_bytes , total_bytes, interrupt_reason " +
                " FROM t_ext_chrome_downloads ");

        preparedStatement.executeUpdate();

    }

    private void transformEmailsTable() throws SQLException {
       // need to use while to populate/decode emails
    }



    private void cleanTables() throws SQLException {
        Statement stmt = DataWareHouseConnection.getDatawarehouseConnection().createStatement();
        stmt.execute("DELETE FROM t_clean_url;");
        stmt.execute("DELETE FROM t_clean_downloads;");
    }

    public static void tranform() {
        new Transformator();
    }


}
