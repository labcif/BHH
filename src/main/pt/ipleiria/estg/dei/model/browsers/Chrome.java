package main.pt.ipleiria.estg.dei.model.browsers;

import main.pt.ipleiria.estg.dei.db.etl.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.utils.Logger;
import org.sleuthkit.autopsy.ingest.IngestJobContext;

import java.net.URLDecoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chrome extends Browser {
    private Logger<Chrome> logger = new Logger<>(Chrome.class);

    public Chrome(IngestJobContext context) {
        super(context);
    }

    @Override
    public void extractAllTables() throws ConnectionException {
        extractHistory();
    }

    private void extractHistory() throws ConnectionException {
        extractTable("t_ext_chrome_urls", "urls", EXTERNAL_URL);
        extractTable("t_ext_chrome_visits", "visits", EXTERNAL_URL);
        extractTable("t_ext_chrome_visit_source", "visit_source", EXTERNAL_URL);
        extractTable("t_ext_chrome_downloads", "downloads", EXTERNAL_URL);
        extractTable("t_ext_chrome_downloads_slices", "downloads_slices", EXTERNAL_URL);
        extractTable("t_ext_chrome_downloads_url_chains", "downloads_url_chains", EXTERNAL_URL);
        extractTable("t_ext_chrome_keyword_search_terms", "keyword_search_terms", EXTERNAL_URL);
        extractTable("t_ext_chrome_meta", "meta", EXTERNAL_URL);
        extractTable("t_ext_chrome_segment_usage", "segment_usage", EXTERNAL_URL);
        extractTable("t_ext_chrome_segments", "segments", EXTERNAL_URL);
        extractTable("t_ext_chrome_sqlite_sequence", "sqlite_sequence", EXTERNAL_URL);
        extractTable("t_ext_chrome_typed_url_sync_metadata", "typed_url_sync_metadata", EXTERNAL_URL);
        extractTable("t_ext_chrome_login_data", "logins", EXTERNAL_URL);
    }

    @Override
    public void runTransformation(String user) throws ConnectionException {
        transformAllTables(user);
    }

    @Override
    public void transformAllTables(String user) throws ConnectionException {
        transformUrlTable(user);
        transformDownloadsTable(user);
        transformEmailsTable(user);
        transformWordsTable(user);
    }

    @Override
    public void deleteExtractTables() throws ConnectionException {
        try {
            Statement stmt = DataWarehouseConnection.getConnection().createStatement();
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
        } catch (SQLException | ClassNotFoundException e) {
            throw new ExtractionException(getModuleName(), "t_ext_", "Error cleaning extracted tables - " + e.getMessage());
        }
    }

    private void transformUrlTable(String user) {
        try {
            PreparedStatement preparedStatement = DataWarehouseConnection.getConnection().prepareStatement(
                    "INSERT INTO t_clean_url (url_full, url_domain, url_path, url_title, url_typed_count, " +
                            "url_visit_time, url_user_origin, url_browser_origin, url_visit_duration ) " +
                            "SELECT teu.url as url_full, " +
                            "replace( SUBSTR( substr(teu.url, instr(teu.url, '://')+3), 0, instr(substr(teu.url, instr(teu.url, '://')+3),'/')), 'www.', '') as url_domain, " +
                            "'TODO: path', title as url_title, typed_count as url_typed_count, " +
                            "strftime('%Y-%m-%d  %H:%M:%S', datetime(((visit_time/1000000)-11644473600), 'unixepoch')) as url_visit_time, " +
                            "'" + user + "',  '" + getModuleName() + "', visit_duration " +
                            "FROM t_ext_chrome_urls teu, t_ext_chrome_visits tev " +
                            "WHERE teu.id = tev.url " +
                            "and url_domain <> ''; ");
            preparedStatement.executeUpdate();
        } catch (SQLException | ClassNotFoundException | ConnectionException e) {
            throw new ExtractionException(getModuleName(), "t_clean_url", "Error cleaning extracted - " + e.getMessage());
        }

    }

    private void transformDownloadsTable(String user) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = DataWarehouseConnection.getConnection().prepareStatement(
                    " INSERT INTO t_clean_downloads (url_domain, " +
                                                        "url_full," +
                                                        " type, " +
                                                        "danger_type," +
                                                        " tab_url, " +
                                                        "download_target_path," +
                                                        " beginning_date, " +
                                                        "ending_date, " +
                                                        "received_bytes," +
                                                        " total_bytes, " +
                                                        "interrupt_reason, " +
                                                        "url_user_origin," +
                                                        " url_browser_origin) " +
                            " SELECT  replace( SUBSTR( substr(site_url,instr(site_url, '://')+3), 0, instr(substr(site_url,instr(site_url, '://')+3),'/')), 'www.', '') as url_domain, " +
                                        "referrer as url_full, " +
                                        "mime_type as type,  " +
                                        "danger_type,  " +
                                        "tab_url , " +
                                        "target_path as download_traget_path, " +
                                        "start_time as begining_date, " +
                                        "end_time as end_date ,  " +
                                        "received_bytes ,  " +
                                        "total_bytes,  " +
                                        "interrupt_reason," +
                                        "'" + user + "',  " +
                                        "'" + getModuleName() + "'" +
                            " FROM t_ext_chrome_downloads ");
            preparedStatement.executeUpdate();
        } catch (SQLException | ClassNotFoundException | ConnectionException e) {
            throw new ExtractionException(getModuleName(), "t_clean_downloads", "Error cleaning extracted - " + e.getMessage());
        }
    }


    private void transformEmailsTable(String user) {
        try {
            Statement statement = DataWarehouseConnection.getConnection().createStatement();
            ResultSet rs = statement.executeQuery(
                    "SELECT  teu.url || ' ' ||  title as url, replace( SUBSTR( substr(teu.url,instr(teu.url, '://')+3), 0,instr(substr(teu.url,instr(teu.url, '://')+3),'/')), 'www.', '') as url_domain" +
                    " FROM t_ext_chrome_urls teu, t_ext_chrome_visits tev " +
                    " WHERE teu.id = tev.url " +
                    " and url_domain <> '' ");

            PreparedStatement preparedStatement =  DataWarehouseConnection.getConnection().prepareStatement(
                    " INSERT INTO t_clean_emails (email, source_full, original_url, username_value, available_password, date, url_user_origin, url_browser_origin) " +
                            " VALUES (?,?,?,?,?,?,?,?)");

            Pattern emailVerification = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");

            Matcher email;
            String encoded, substring, decode;
            
            while(rs.next()){
                encoded = rs.getString("url");
                substring = encoded.substring(0, encoded.contains("&") ? encoded.indexOf("&") : encoded.length() -1);
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
                    preparedStatement.setString(3, null);//original_url
                    preparedStatement.setString(4, null);//username_value
                    preparedStatement.setString(5, null);//available_password
                    preparedStatement.setString(6, null);//date
                    preparedStatement.setString(7, user);
                    preparedStatement.setString(8, getModuleName());
                    preparedStatement.addBatch();
                }
            }
            preparedStatement.executeBatch();

            rs = statement.executeQuery(" SELECT  origin_url, username_value , " +
                                        " ifnull(password_value, false) as available_password, " +
                                        "date_created as date " +
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
                    preparedStatement.setString(5, "");
                    preparedStatement.setString(6, rs.getString("date"));
                    preparedStatement.setString(7, user);
                    preparedStatement.setString(8, getModuleName());
                    preparedStatement.addBatch();
                }
            }
            preparedStatement.executeBatch();
        } catch (SQLException | ClassNotFoundException | ConnectionException e) {
            throw new ExtractionException(getModuleName(), "t_clean_emails", "Error cleaning extracted - " + e.getMessage());
        }
    }


    private void transformWordsTable(String user){
        try {
            Statement statement = DataWarehouseConnection.getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT substr(url, instr(url, '?q=')+3) as word, url as url_full," +
                                            "replace( SUBSTR( substr(url, instr(url, '://')+3), 0, instr(substr(url, instr(url, '://')+3),'/')), 'www.', '') as url_domain " +
                    "FROM t_ext_chrome_urls " +
                    "where url like '%google.%' and url like '%?q=%'");
            insertWordInTable(rs, user);
        }catch (SQLException | ClassNotFoundException | ConnectionException e) {
            throw new ExtractionException(getModuleName(), "t_clean_words", "Error cleaning extracted - " + e.getMessage());
        }
    }

    public String getModuleName() {
        return "Google_Chrome";
    }

    @Override
    public String getPathToBrowserHistory() {
        return "AppData/Local/Google/Chrome/User Data/Default";
    }

    @Override
    public String getHistoryFilename() {
        return "History";
    }

    @Override
    public String getLoginDataFilename() {
        return "Login Data";
    }

}
