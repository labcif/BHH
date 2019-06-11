package main.pt.ipleiria.estg.dei.labcif.bhh.modules;

import main.pt.ipleiria.estg.dei.labcif.bhh.database.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.OperatingSystemNotSupportedException;
import main.pt.ipleiria.estg.dei.labcif.bhh.models.LoginOriginEnum;
import main.pt.ipleiria.estg.dei.labcif.bhh.utils.LoggerBHH;
import org.sleuthkit.autopsy.ingest.IngestJobContext;

import java.io.File;
import java.net.URLDecoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static main.pt.ipleiria.estg.dei.labcif.bhh.utils.OperatingSystemUtils.*;

public class ChromeModule extends BrowserModule {
    private LoggerBHH<ChromeModule> loggerBHH = new LoggerBHH<>(ChromeModule.class);

    public ChromeModule(IngestJobContext context, String databaseDirectory) {
        super(context, databaseDirectory);
    }

    public ChromeModule(String databaseDirectory) {
        super(databaseDirectory);
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
    public void runTransformation(String user, String profileName, String fullLocationFile) throws ConnectionException {
        transformAllTables(user, profileName, fullLocationFile);
    }

    @Override
    public void transformAllTables(String user, String profileName,  String fullLocationFile) throws ConnectionException {
        transformUrlTable(user, profileName, fullLocationFile);
        transformDownloadsTable(user, profileName, fullLocationFile);
        transformEmailsTable(user, profileName, fullLocationFile);
        transformWordsTable(user, profileName, fullLocationFile);
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

    private void transformUrlTable(String user, String profileName, String fullLocationFile) {
        try {
            PreparedStatement preparedStatement = DataWarehouseConnection.getConnection(databaseDirectory).prepareStatement(
                    "INSERT INTO t_clean_url (url_full, url_domain, url_path, url_title, url_typed, url_visit_full_date_start, url_visit_date_start, " +
                                                "url_visit_time_start, url_user_origin, url_browser_origin, url_visit_duration, url_natural_key, url_visit_full_date_end, " +
                                                "url_visit_date_end, url_visit_time_end, url_hidden, url_profile_name, url_filename_location) " +
                            "SELECT teu.url as url_full, " +
                            extractDomainFromFullUrlInSqliteQuery("teu.url", "url_domain") + ", " +
                            "substr( replace(teu.url, SUBSTR( substr(teu.url, instr(teu.url, '://')+3), 0, instr(substr(teu.url, instr(teu.url, '://')+3),'/')), ''), instr(teu.url, '://')+3) as url_path, " +
                            "title as url_title, " +
                            "case when typed_count > 0 then 1 else 0 end as url_typed, " +
                            extractDateFromColumn("visit_time", "url_visit_full_date_start", FULL_DATE_FORMAT) + ", " +
                            extractDateFromColumn("visit_time", "url_visit_date_start", DATE_FORMAT) + ", " +
                            extractDateFromColumn("visit_time", "url_visit_time_start", TIME_FORMAT) + ", " +
                            "'" + user + "', " +
                            "'" + getModuleName() + "', " +
                            extractDateFromColumn("visit_duration", "url_visit_duration", TIME_FORMAT) + ", " +
                            "teu.id as url_natural_key, " +
                            "strftime('" + FULL_DATE_FORMAT + "' , datetime(strftime('" + FULL_DATE_FORMAT + "', datetime(((visit_time/1000000)-11644473600), 'unixepoch')), '+' || strftime('" + TIME_FORMAT +"', datetime(((visit_duration/1000000)-11644473600), 'unixepoch')))) as url_visit_end, " +
                            "strftime('" + DATE_FORMAT + "' ,datetime(strftime('" + FULL_DATE_FORMAT + "', datetime(((visit_time/1000000)-11644473600), 'unixepoch')), '+' || strftime('" + TIME_FORMAT +"', datetime(((visit_duration/1000000)-11644473600), 'unixepoch')))) as url_visit_end, " +
                            "strftime('" + TIME_FORMAT + "' ,datetime(strftime('" + FULL_DATE_FORMAT + "', datetime(((visit_time/1000000)-11644473600), 'unixepoch')), '+' || strftime('" + TIME_FORMAT +"', datetime(((visit_duration/1000000)-11644473600), 'unixepoch')))) as url_visit_end," +
                            "hidden as ur_hidden," +
                            "'" + profileName + "', " +
                            "'" + fullLocationFile + "' " +
                            "FROM t_ext_chrome_urls teu, t_ext_chrome_visits tev " +
                            "WHERE teu.id = tev.url " +
                            "and url_domain <> ''; ");
            preparedStatement.executeUpdate();
        } catch (SQLException | ClassNotFoundException | ConnectionException e) {
            throw new ExtractionException(getModuleName(), "t_clean_url", "Error cleaning extracted - " + e.getMessage());
        }

    }

    private void transformDownloadsTable(String user, String profileName, String fullLocationFile) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = DataWarehouseConnection.getConnection(databaseDirectory).prepareStatement(
                    " INSERT INTO t_clean_downloads (downloads_natural_key, " +
                                                        "downloads_domain, " +
                                                        "downloads_full_url, " +
                                                        "downloads_mime_type,  " +
                                                        "downloads_target_path, " +
                                                        "downloads_beginning_full_date,  " +
                                                        "downloads_beginning_date,  " +
                                                        "downloads_beginning_time,  " +
                                                        "downloads_ending_full_date,  " +
                                                        "downloads_ending_date,  " +
                                                        "downloads_ending_time,  " +
                                                        "downloads_received_bytes, " +
                                                        "downloads_total_bytes,  " +
                                                        "downloads_user_origin, " +
                                                        "downloads_browser_origin," +
                                                        "downloads_profile_name," +
                                                        "downloads_filename_location) " +
                                " SELECT id as downloads_natural_key, " +
                                        extractDomainFromFullUrlInSqliteQuery("referrer", "downloads_domain") + ", " +
                                        "referrer as downloads_full_url, " +
                                        "mime_type as downloads_mime_type,  " +
                                        "target_path as downloads_target_path, " +
                                        extractDateFromColumn("start_time", "downloads_beginning_full_date", FULL_DATE_FORMAT) + ", " +
                                        extractDateFromColumn("start_time", "downloads_beginning_date", DATE_FORMAT) + ", " +
                                        extractDateFromColumn("start_time", "downloads_beginning_time", TIME_FORMAT) + ", " +
                                        extractDateFromColumn("end_time", "downloads_ending_full_date", FULL_DATE_FORMAT) + ", " +
                                        extractDateFromColumn("end_time", "downloads_ending_date", DATE_FORMAT) + ", " +
                                        extractDateFromColumn("end_time", "downloads_ending_time", TIME_FORMAT) + ", " +
                                        "received_bytes as downloads_received_bytes,  " +
                                        "total_bytes as downloads_total_bytes,  " +
                                        "'" + user + "',  " +
                                        "'" + getModuleName() + "', " +
                                        "'" + profileName + "', " +
                                        "'" + fullLocationFile + "' " +
                                " FROM t_ext_chrome_downloads ");
            preparedStatement.executeUpdate();
        } catch (SQLException | ClassNotFoundException | ConnectionException e) {
            throw new ExtractionException(getModuleName(), "t_clean_downloads", "Error cleaning extracted - " + e.getMessage());
        }
    }


    private void transformEmailsTable(String user, String profileName, String fullLocationFile) {
        try {
            Statement statement = DataWarehouseConnection.getConnection(databaseDirectory).createStatement();

            ResultSet rs = statement.executeQuery(
                    "SELECT  teu.url || ' ' ||  title as url, replace( SUBSTR( substr(teu.url,instr(teu.url, '://')+3), 0,instr(substr(teu.url,instr(teu.url, '://')+3),'/')), 'www.', '') as url_domain" +
                    " FROM t_ext_chrome_urls teu, t_ext_chrome_visits tev " +
                    " WHERE teu.id = tev.url " +
                    " and url_domain <> '' ");

            PreparedStatement preparedStatement =  DataWarehouseConnection.getConnection(databaseDirectory).prepareStatement(
                    " INSERT INTO t_clean_logins (logins_email, logins_domain, logins_username_value, logins_available_password, logins_date, logins_user_origin, logins_browser_origin, logins_table_origin, logins_profile_name, logins_filename_location) " +
                            " VALUES (?,?,?,?,?,?,?,?,?,?)");

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
                    preparedStatement.setString(9, profileName);
                    preparedStatement.setString(10, fullLocationFile);
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
                    preparedStatement.setString(9, profileName);
                    preparedStatement.setString(10, fullLocationFile);
                    preparedStatement.addBatch();
                }
            }
            preparedStatement.executeBatch();

        } catch (SQLException | ClassNotFoundException | ConnectionException e) {
            throw new ExtractionException(getModuleName(), "t_clean_logins", "Error cleaning extracted - " + e.getMessage());
        }
    }


    private void transformWordsTable(String user, String profileName, String fullLocationFile){
        try {
            Statement statement = DataWarehouseConnection.getConnection(databaseDirectory).createStatement();
            ResultSet rs = statement.executeQuery("SELECT substr(url, instr(url, '?q=')+3) as word, url as url_full," +
                                            "replace( SUBSTR( substr(url, instr(url, '://')+3), 0, instr(substr(url, instr(url, '://')+3),'/')), 'www.', '') as url_domain " +
                    "FROM t_ext_chrome_urls " +
                    "where url like '%google.%' and url like '%?q=%'");
            insertWordInTable(rs, user, profileName, fullLocationFile);
        }catch (SQLException | ClassNotFoundException | ConnectionException e) {
            throw new ExtractionException(getModuleName(), "t_clean_search_in_engines", "Error cleaning extracted - " + e.getMessage());
        }
    }

    protected String extractDateFromColumn(String oldColumn, String newColumn, String format) {
        return "strftime('" + format + "', datetime(((" + oldColumn +"/1000000)-11644473600), 'unixepoch')) as " + newColumn;
    }

    public String getModuleName() {
        return "GOOGLE_CHROME";
    }

    @Override
    public String getPathToBrowserInstallation() {
        return "AppData/Local/Google/Chrome/User Data/Default";//TODO: This is probably wrong... because this way we are not having other profiles into consideration
    }

    @Override
    public String getFullPathToBrowserInstallationInCurrentMachine() {
        if (isWindows()) {
            return getRoot() + "Users\\" + USER + "\\AppData\\Local\\Google\\Chrome\\User Data";
        } else if (isUnix()) {
            return "TODO"; //TODO: implement linux directory
        } else {
            throw new OperatingSystemNotSupportedException();
        }
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
