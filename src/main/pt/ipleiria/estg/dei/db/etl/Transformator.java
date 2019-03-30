package main.pt.ipleiria.estg.dei.db.etl;

import main.pt.ipleiria.estg.dei.exceptions.TransformationException;
import main.pt.ipleiria.estg.dei.utils.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

            Statement statement = DataWarehouseConnection.getDatawarehouseConnection().createStatement();

            cleanTables();
            transformUrlTable();
            transformDownloadsTable();
            transformBlockedTable();
            transformEmailsTable(statement);
            transformWordsTable(statement);

            DataWarehouseConnection.getDatawarehouseConnection().commit();

        } catch (SQLException  e) {
            logger.error(e.getMessage());
            throw new TransformationException("Could't connect to database. Reason: " + e.getMessage());
        }
    }

    private void transformUrlTable() throws SQLException {
        PreparedStatement preparedStatement = DataWarehouseConnection.getDatawarehouseConnection().prepareStatement(
                " INSERT INTO t_clean_url (url_full, url_domain, url_path, url_title, url_visit_count, url_typed_count, url_visit_time ) " +
                        "SELECT  teu.url as url_full, replace( SUBSTR( substr(teu.url,instr(teu.url, '://')+3), 0,instr(substr(teu.url,instr(teu.url, '://')+3),'/')), 'www.', '') as url_domain, 'TODO: path',title as url_title," +
                        " visit_count as url_visit_count, typed_count as url_typed_count, strftime('%d-%m-%Y', datetime(((visit_time/1000000)-11644473600), 'unixepoch')) as url_visit_time" +
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

    private void transformBlockedTable() throws SQLException {
        PreparedStatement preparedStatement = DataWarehouseConnection.getDatawarehouseConnection().prepareStatement(
                " INSERT INTO t_clean_blocked_websites (domain) " +
                        " SELECT  domain " +
                        " FROM t_ext_blocked_websites ");

        preparedStatement.executeUpdate();
    }

    //Todo this isn´t finished (needs further discussion)
    private void transformEmailsTable(Statement statement) throws SQLException {
        ResultSet rs;

        rs = statement.executeQuery("SELECT  teu.url || ' ' ||  title as url, replace( SUBSTR( substr(teu.url,instr(teu.url, '://')+3), 0,instr(substr(teu.url,instr(teu.url, '://')+3),'/')), 'www.', '') as url_domain" +
                " FROM t_ext_chrome_urls teu, t_ext_chrome_visits tev " +
                " WHERE teu.id = tev.url " +
                " and url_domain <> '' ");

        PreparedStatement preparedStatement =  DataWarehouseConnection.getDatawarehouseConnection().prepareStatement(
                " INSERT INTO t_clean_emails (email, source_full, original_url, username_value, available_password, date) " +
                        " VALUES (?,?,?,?,?,?)");

        Pattern emailVerification = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");

        Matcher email;
        String encoded, substring, decode;

        while(rs.next()){
            encoded = rs.getString("url");
            substring = encoded.substring(0, encoded.indexOf("&") != -1 ? encoded.indexOf("&") : encoded.length() -1);
            //In case the string has been decoded already
            try {
                decode = URLDecoder.decode(substring, "UTF-8"); }
            catch(Exception ex) {
                decode = substring;
            }
            email = emailVerification.matcher(decode);
            while (email.find()) {
                preparedStatement.setString(1, email.group());
                preparedStatement.setString(2, rs.getString("url_domain"));
                preparedStatement.setString(3, null);
                preparedStatement.setString(4, null);
                preparedStatement.setString(5, null);
                preparedStatement.setString(6, null);
                preparedStatement.addBatch();
            }
        }
        preparedStatement.executeBatch();

        rs = statement.executeQuery(" SELECT  origin_url, username_value ,  ifnull(password_value, false) as available_password, date_created as date " +
                "  FROM t_ext_chrome_login_data");


        String username_value;
        while(rs.next()){
            username_value = rs.getString("username_value");
            email = emailVerification.matcher(username_value);
            while (email.find()) {
                preparedStatement.setString(1, email.group());
                preparedStatement.setString(2, null);
                preparedStatement.setString(3, rs.getString("origin_url"));
                preparedStatement.setString(4, rs.getString("username_value"));
                preparedStatement.setString(5, rs.getString("available_password"));
                preparedStatement.setString(6, rs.getString("date"));
                preparedStatement.addBatch();
            }
        }
        preparedStatement.executeBatch();

    }

    //Todo this isn´t finished (needs further discussion)
    public void transformWordsTable(Statement statement) throws SQLException{

        ResultSet rs = statement.executeQuery("SELECT substr(url, instr(url, '?q=')+3) as word, url as url_full  " +
                "FROM t_ext_chrome_urls " +
                "where url like '%google.%' and url like '%?q=%'");

        PreparedStatement preparedStatement =  DataWarehouseConnection.getDatawarehouseConnection().prepareStatement(
                " INSERT INTO t_clean_words (word, source_full) " +
                        " VALUES (?,?)");

        String encoded;
        String substring;
        String decode;
        String[] words;

        while (rs.next()) {
            encoded = rs.getString("word");
            substring = encoded.substring(0, encoded.indexOf("&") != -1 ? encoded.indexOf("&") : encoded.length() -1);

            //In case the string has been decoded already
            try {
                decode = URLDecoder.decode(substring, "UTF-8"); }
            catch(Exception ex) {
                decode = substring;
            }
            words = decode.split("\\s+");
            for (String s: words) {
                preparedStatement.setString(1, s);
                preparedStatement.setString(2, rs.getString("url_full"));
                preparedStatement.addBatch();
            }
        }

        preparedStatement.executeBatch();
    }


    private void cleanTables() throws SQLException {
        Statement stmt = DataWarehouseConnection.getDatawarehouseConnection().createStatement();
        stmt.execute("DELETE FROM t_clean_url;");
        stmt.execute("DELETE FROM t_clean_downloads;");
        stmt.execute("DELETE FROM t_clean_blocked_websites;");
        stmt.execute("DELETE FROM t_clean_emails;");
        stmt.execute("DELETE FROM t_clean_words;");
    }

    public static void tranform() {
        new Transformator();
    }


}