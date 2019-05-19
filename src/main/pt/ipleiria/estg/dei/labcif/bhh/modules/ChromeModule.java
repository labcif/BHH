package main.pt.ipleiria.estg.dei.labcif.bhh.modules;

import main.pt.ipleiria.estg.dei.labcif.bhh.database.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.models.LoginOriginEnum;
import main.pt.ipleiria.estg.dei.labcif.bhh.utils.LoggerBHH;
import org.sleuthkit.autopsy.ingest.IngestJobContext;

import java.net.URLDecoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChromeModule extends BrowserModule {
    private LoggerBHH<ChromeModule> loggerBHH = new LoggerBHH<>(ChromeModule.class);

    public ChromeModule(IngestJobContext context, String databaseDirectory) {
        super(context, databaseDirectory);
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
        extractTable("t_ext_chrome_segment_usage", "segment_usage", EXTERNAL_URL);
        extractTable("t_ext_chrome_segments", "segments", EXTERNAL_URL);
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
            Statement stmt = DataWarehouseConnection.getConnection(databaseDirectory).createStatement();
            stmt.execute("DELETE FROM t_ext_chrome_urls;");
            stmt.execute("DELETE FROM t_ext_chrome_visits;");
            stmt.execute("DELETE FROM t_ext_chrome_visit_source;");
            stmt.execute("DELETE FROM t_ext_chrome_downloads;");
            stmt.execute("DELETE FROM t_ext_chrome_downloads_slices;");
            stmt.execute("DELETE FROM t_ext_chrome_downloads_url_chains;");
            stmt.execute("DELETE FROM t_ext_chrome_keyword_search_terms;");
            stmt.execute("DELETE FROM t_ext_chrome_segment_usage;");
            stmt.execute("DELETE FROM t_ext_chrome_segments;");
            stmt.execute("DELETE FROM t_ext_chrome_typed_url_sync_metadata;");
            stmt.execute("DELETE FROM t_ext_chrome_login_data;");
        } catch (SQLException | ClassNotFoundException e) {
            throw new ExtractionException(getModuleName(), "t_ext_", "Error cleaning extracted tables - " + e.getMessage());
        }
    }

    private void transformUrlTable(String user) {
        try {
            PreparedStatement preparedStatement = DataWarehouseConnection.getConnection(databaseDirectory).prepareStatement(
                    "INSERT INTO t_clean_url (url_full, url_domain, url_path, url_title, url_typed, url_visit_full_date_start, url_visit_date_start, " +
                                                "url_visit_time_start, url_user_origin, url_browser_origin, url_visit_duration, url_natural_key, url_visit_full_date_end, " +
                                                "url_visit_date_end, url_visit_time_end, url_hidden ) " +
                            "SELECT teu.url as url_full, " +
                            "replace( SUBSTR( substr(teu.url, instr(teu.url, '://')+3), 0, instr(substr(teu.url, instr(teu.url, '://')+3),'/')), 'www.', '') as url_domain, " +
                            "substr( replace(teu.url, SUBSTR( substr(teu.url, instr(teu.url, '://')+3), 0, instr(substr(teu.url, instr(teu.url, '://')+3),'/')), ''), instr(teu.url, '://')+3) as url_path, " +
                            "title as url_title, " +
                            "case when typed_count > 0 then 1 else 0 end as url_typed, " +
                            "strftime('%Y-%m-%d  %H:%M:%S', datetime(((visit_time/1000000)-11644473600), 'unixepoch')) as url_visit_full_date_start, " +
                            "strftime('%Y-%m-%d', datetime(((visit_time/1000000)-11644473600), 'unixepoch')) as url_visit_date_start, " +
                            "strftime('%H:%M:%S', datetime(((visit_time/1000000)-11644473600), 'unixepoch')) as url_visit_time_start, " +
                            "'" + user + "', " +
                            "'" + getModuleName() + "', " +
                            "strftime('%H:%M:%S', datetime(((visit_duration/1000000)-11644473600), 'unixepoch')) as url_visit_duration, " +
                            "teu.id, " +
                            "strftime('%Y-%m-%d  %H:%M:%S' ,datetime(strftime('%Y-%m-%d  %H:%M:%S', datetime(((visit_time/1000000)-11644473600), 'unixepoch')), '+' || strftime('%H:%M:%S', datetime(((visit_duration/1000000)-11644473600), 'unixepoch')))) as url_visit_end, " +
                            "strftime('%Y-%m-%d' ,datetime(strftime('%Y-%m-%d  %H:%M:%S', datetime(((visit_time/1000000)-11644473600), 'unixepoch')), '+' || strftime('%H:%M:%S', datetime(((visit_duration/1000000)-11644473600), 'unixepoch')))) as url_visit_end, " +
                            "strftime('%H:%M:%S' ,datetime(strftime('%Y-%m-%d  %H:%M:%S', datetime(((visit_time/1000000)-11644473600), 'unixepoch')), '+' || strftime('%H:%M:%S', datetime(((visit_duration/1000000)-11644473600), 'unixepoch')))) as url_visit_end," +
                            "hidden as ur_hidden " +
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
            preparedStatement = DataWarehouseConnection.getConnection(databaseDirectory).prepareStatement(
                    " INSERT INTO t_clean_downloads (downloads_url_domain, " +
                                                        "downloads_url_full, " +
                                                        "downloads_type,  " +
                                                        "downloads_danger_type, " +
                                                        "downloads_tab_url,  " +
                                                        "downloads_download_target_path, " +
                                                        "downloads_beginning_date,  " +
                                                        "downloads_ending_date,  " +
                                                        "downloads_received_bytes, " +
                                                        "downloads_total_bytes,  " +
                                                        "downloads_interrupt_reason,  " +
                                                        "downloads_user_origin, " +
                                                        "downloads_browser_origin) " +
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
            Statement statement = DataWarehouseConnection.getConnection(databaseDirectory).createStatement();

            ResultSet rs = statement.executeQuery(
                    "SELECT  teu.url || ' ' ||  title as url, replace( SUBSTR( substr(teu.url,instr(teu.url, '://')+3), 0,instr(substr(teu.url,instr(teu.url, '://')+3),'/')), 'www.', '') as url_domain" +
                    " FROM t_ext_chrome_urls teu, t_ext_chrome_visits tev " +
                    " WHERE teu.id = tev.url " +
                    " and url_domain <> '' ");

            PreparedStatement preparedStatement =  DataWarehouseConnection.getConnection(databaseDirectory).prepareStatement(
                    " INSERT INTO t_clean_logins (logins_email, logins_domain, logins_username_value, logins_available_password, logins_date, logins_user_origin, logins_browser_origin, logins_table_origin) " +
                            " VALUES (?,?,?,?,?,?,?,?)");

            Pattern emailVerification = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");

            Matcher email;
            String encoded, substring, decode;
            
            while(rs.next()){
                encoded = rs.getString("url");

                //In case the string has been decoded already
                substring = encoded.contains("&") ?  encoded.substring(0, encoded.indexOf("&")) : encoded;

                try {
                    decode = URLDecoder.decode(substring, "UTF-8"); }
                catch(Exception ex) {
                    decode = substring;
                }
                email = emailVerification.matcher(decode);
                while (email.find()) {
                    preparedStatement.setString(1, email.group());
                    preparedStatement.setString(2, rs.getString("url_domain"));
                    preparedStatement.setString(3, null);//username_value
                    preparedStatement.setString(4, null);//available_password
                    preparedStatement.setString(5, null);//date
                    preparedStatement.setString(6, user);
                    preparedStatement.setString(7, getModuleName());
                    preparedStatement.setString(8, LoginOriginEnum.URL_ORIGIN.name());
                    preparedStatement.addBatch();
                }
            }
            preparedStatement.executeBatch();

            //Logins found on table logins
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
                    preparedStatement.setString(2, rs.getString("origin_url"));
                    preparedStatement.setString(3, rs.getString("username_value"));
                    preparedStatement.setString(4, "");
                    preparedStatement.setString(5, rs.getString("date"));
                    preparedStatement.setString(6, user);
                    preparedStatement.setString(7, getModuleName());
                    preparedStatement.setString(8, LoginOriginEnum.LOGIN_ORIGIN.name());
                    preparedStatement.addBatch();
                }
            }
            preparedStatement.executeBatch();

        } catch (SQLException | ClassNotFoundException | ConnectionException e) {
            throw new ExtractionException(getModuleName(), "t_clean_logins", "Error cleaning extracted - " + e.getMessage());
        }
    }


    private void transformWordsTable(String user){
        try {
            Statement statement = DataWarehouseConnection.getConnection(databaseDirectory).createStatement();
            ResultSet rs = statement.executeQuery("SELECT substr(url, instr(url, '?q=')+3) as word, url as url_full," +
                                            "replace( SUBSTR( substr(url, instr(url, '://')+3), 0, instr(substr(url, instr(url, '://')+3),'/')), 'www.', '') as url_domain " +
                    "FROM t_ext_chrome_urls " +
                    "where url like '%google.%' and url like '%?q=%'");
            insertWordInTable(rs, user);
        }catch (SQLException | ClassNotFoundException | ConnectionException e) {
            throw new ExtractionException(getModuleName(), "t_clean_search_in_engines", "Error cleaning extracted - " + e.getMessage());
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
