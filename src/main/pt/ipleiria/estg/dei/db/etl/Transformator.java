package main.pt.ipleiria.estg.dei.db.etl;

import main.pt.ipleiria.estg.dei.exceptions.TransformationException;
import main.pt.ipleiria.estg.dei.utils.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Transformator {

    private Logger<Transformator> logger = new Logger<>(Transformator.class);

    private Transformator() {
        try {
            cleanTables();
            transformUrlTable();
            transformDownloadsTable();
            transformEmailsTable();

            DataWarehouseConnection.getDatawarehouseConnection().commit();

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

    //Todo this isn´t finished
    private void transformEmailsTable() throws SQLException {
        ResultSet rs;
        Statement statement = DataWarehouseConnection.getDatawarehouseConnection().createStatement();


        rs = statement.executeQuery("SELECT  teu.url as url_full, replace( SUBSTR( substr(teu.url,instr(teu.url, '://')+3), 0,instr(substr(teu.url,instr(teu.url, '://')+3),'/')), 'www.', '') as url_domain, " +
                "   title as url_title" +
                "  FROM t_ext_chrome_urls teu, t_ext_chrome_visits tev" +
                "  WHERE teu.id = tev.url " +
                "  and url_domain <> '' ");

        PreparedStatement preparedStatement =  DataWarehouseConnection.getDatawarehouseConnection().prepareStatement(
                " INSERT INTO t_clean_emails (email, source_full, original_url, username_value, available_password, date) " +
                        " VALUES (?,?,?,?,?,?)");

        Pattern emailVerification = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
        String emailUrl;
        Matcher m;
        while(rs.next()){
            emailUrl = rs.getString("url_full");
            m = emailVerification.matcher(emailUrl);
            while (m.find()) {
                preparedStatement.setString(1, m.group());
                preparedStatement.setString(2, rs.getString("url_full"));
                preparedStatement.setString(3, null);
                preparedStatement.setString(4, null);
                preparedStatement.setString(5, null);
                preparedStatement.setString(6, null);
                preparedStatement.addBatch();
            }
        }
        preparedStatement.executeBatch();
        
        rs = statement.executeQuery(" SELECT  origin_url, username_value ,  password_value as available_password, date_created as date " +
                "  FROM t_ext_chrome_login_data");

        while(rs.next()){
            emailUrl = rs.getString("username_value");
            m = emailVerification.matcher(emailUrl);
            while (m.find()) {
                preparedStatement.setString(1, m.group());
                preparedStatement.setString(2, null);
                preparedStatement.setString(3, rs.getString("origin_url"));
                preparedStatement.setString(4, rs.getString("username_value"));
                preparedStatement.setString(5, "Todo");
                preparedStatement.setString(6, rs.getString("date"));
                preparedStatement.addBatch();
            }
        }
        preparedStatement.executeBatch();
    }

    private void cleanTables() throws SQLException {
        Statement stmt = DataWarehouseConnection.getDatawarehouseConnection().createStatement();
        stmt.execute("DELETE FROM t_clean_url;");
        stmt.execute("DELETE FROM t_clean_downloads;");
        stmt.execute("DELETE FROM t_clean_emails;");
    }

    public static void tranform() {
        new Transformator();
    }


}
