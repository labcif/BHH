package main.pt.ipleiria.estg.dei.labcif.bhh.modules;

import main.pt.ipleiria.estg.dei.labcif.bhh.database.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.TransformationException;
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

public class FirefoxModule extends BrowserModule {
    private LoggerBHH<FirefoxModule> loggerBHH = new LoggerBHH<>(FirefoxModule.class);

    public FirefoxModule(IngestJobContext context, String databaseDirectory) {
        super(context, databaseDirectory);
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
    }

    @Override
    public void transformAllTables(String user) throws ConnectionException {
        transformUrlTable(user);
        transformWordsTable(user);
        transformEmailsTable(user);
        //TODO: missing transformDOwnload Tables.
    }

    @Override
    public void deleteExtractTables() throws ConnectionException {
        try {
            Statement stmt = DataWarehouseConnection.getConnection(databaseDirectory).createStatement();
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
        } catch (SQLException | ClassNotFoundException e) {
            throw new ExtractionException(getModuleName(), "t_ext_","Error cleaning extracted tables - " + e.getMessage());
        }
    }

    private void transformUrlTable(String user) throws ConnectionException {
        try {
            PreparedStatement preparedStatement = DataWarehouseConnection.getConnection(databaseDirectory).prepareStatement(
                    "INSERT INTO t_clean_url (url_full, url_domain, url_path, url_title, url_typed, " +
                                                    "url_visit_full_date_start, url_visit_date_start, url_visit_time_start, " +
                                                    "url_user_origin, url_browser_origin, url_visit_duration, url_natural_key, " +
                                                    "url_visit_full_date_end, url_visit_date_end, url_visit_time_end,url_hidden ) " +
                            "SELECT  mp.url as url_full, " +
                                    extractDomainFromFullUrlInSqliteQuery("mp.url", "url_domain") + ", " +
                                    "replace( SUBSTR( substr(mp.url, instr(mp.url, '://')+3), 0,instr(substr(mp.url, instr(mp.url, '://')+3),'/')), 'www.', '') as url_path, " +
                                    "title as url_title, " +
                                    "typed as url_typed, " +
                                    extractDateFromColumn("mh.visit_date", "url_visit_full_date_start", FULL_DATE_FORMAT) + ", " +
                                    extractDateFromColumn("mh.visit_date", "url_visit_date_start", DATE_FORMAT) + ", " +
                                    extractDateFromColumn("mh.visit_date", "url_visit_time_start", TIME_FORMAT) + ", " +
                                    "'" + user + "', " +
                                    "'" + getModuleName() + "', " +
                                    "0, " + //Firefox does not store this information
                                    "mp.id," +
                                    extractDateFromColumn("mh.visit_date", "url_visit_full_date_end", FULL_DATE_FORMAT) + ", " +
                                    extractDateFromColumn("mh.visit_date", "url_visit_date_end", DATE_FORMAT) + ", " +
                                    extractDateFromColumn("mh.visit_date", "url_visit_time_end", TIME_FORMAT) + ", " +
                                    "hidden as url_hidden " +
                            "FROM t_ext_mozila_places mp, t_ext_mozila_historyvisits mh " +
                            "WHERE mp.id = mh.place_id " +
                            "and url_domain <> ''; ");
            preparedStatement.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            throw new TransformationException(getModuleName(), "t_clean_url","Error transforming tables: " + e.getMessage());
        }
    }

    private void transformWordsTable(String user){
        try {
            Statement statement = DataWarehouseConnection.getConnection(databaseDirectory).createStatement();
            ResultSet rs = statement.executeQuery("SELECT substr(url, instr(url, '?q=')+3) as word, url as url_full," +
                                                "replace( SUBSTR( substr(url, instr(url, '://')+3), 0,instr(substr(url, instr(url, '://')+3),'/')), 'www.', '') as url_domain  " +
                    "FROM t_ext_mozila_places " +
                    "where url like '%google.%' and url like '%?q=%'");


            insertWordInTable(rs, user);

        }catch (SQLException | ClassNotFoundException | ConnectionException e) {
            throw new ExtractionException(getModuleName(), "t_clean_search_in_engines", "Error cleaning extracted - " + e.getMessage());
        }
    }

    private void transformEmailsTable(String user) {
        try {
            Statement statement = DataWarehouseConnection.getConnection(databaseDirectory).createStatement();


            ResultSet rs = statement.executeQuery(
                    "SELECT  mp.url as url_full, " +
                                    "replace( SUBSTR( substr(mp.url, instr(mp.url, '://')+3), 0,instr(substr(mp.url, instr(mp.url, '://')+3),'/')), 'www.', '') as url_domain, " +
                                    "'TODO: path', title as url_title, typed as url_typed_count, " +
                                    "strftime('%Y-%m-%d %H:%M:%S', datetime(mh.visit_date/1000000, 'unixepoch', 'localtime')) as url_visit_time, " +
                                    "'" + user + "', '" + getModuleName() + "' " +
                            "FROM t_ext_mozila_places mp, t_ext_mozila_historyvisits mh " +
                            "WHERE mp.id = mh.place_id " +
                            "and url_domain <> ''; ");

            PreparedStatement preparedStatement =  DataWarehouseConnection.getConnection(databaseDirectory).prepareStatement(
                    " INSERT INTO t_clean_logins (logins_email, logins_domain, logins_username_value, logins_available_password, logins_date, logins_user_origin, logins_browser_origin, logins_table_origin) " +
                            " VALUES (?,?,?,?,?,?,?,?)");

            Pattern emailVerification = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");

            Matcher email;
            String encoded, substring, decode;

            while(rs.next()){
                encoded = rs.getString("url_full");

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

        } catch (SQLException | ClassNotFoundException | ConnectionException e) {
            throw new ExtractionException(getModuleName(), "t_clean_logins", "Error cleaning extracted - " + e.getMessage());
        }
    }

    protected String extractDateFromColumn(String oldColumn, String newColumn, String format) {
        return "strftime('" + format + "', datetime(" + oldColumn + "/1000000, 'unixepoch', 'localtime')) as " + newColumn;
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

    @Override
    public String getLoginDataFilename() {
        return "TODO: not implemented yet";
    }
}
