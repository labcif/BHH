package main.pt.ipleiria.estg.dei.model.browsers;

import main.pt.ipleiria.estg.dei.db.etl.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.exceptions.TransformationException;
import main.pt.ipleiria.estg.dei.utils.Logger;
import org.sleuthkit.autopsy.ingest.IngestJobContext;

import java.net.URLDecoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static main.pt.ipleiria.estg.dei.model.browsers.LoginOriginEnum.URL_ORIGIN;

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
    }

    @Override
    public void transformAllTables(String user) throws ConnectionException {
        transformUrlTable(user);
        transformWordsTable(user);
        transformEmailsTable(user);
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
        } catch (SQLException | ClassNotFoundException e) {
            throw new ExtractionException(getModuleName(), "t_ext_","Error cleaning extracted tables - " + e.getMessage());
        }
    }

    private void transformUrlTable(String user) throws ConnectionException {
        try {
            PreparedStatement preparedStatement = DataWarehouseConnection.getConnection().prepareStatement(
                    "INSERT INTO t_clean_url (url_full, url_domain, url_path, url_title, url_typed_count, " +
                                                    "url_visit_time, url_user_origin, url_browser_origin, url_visit_duration ) " +
                            "SELECT  mp.url as url_full, " +
                                    "replace( SUBSTR( substr(mp.url, instr(mp.url, '://')+3), 0,instr(substr(mp.url, instr(mp.url, '://')+3),'/')), 'www.', '') as url_domain, " +
                                    "'TODO: path', title as url_title, typed as url_typed_count, " +
                                    "strftime('%Y-%m-%d %H:%M:%S', datetime(mh.visit_date/1000000, 'unixepoch', 'localtime')) as url_visit_time, " +
                                    "'" + user + "', '" + getModuleName() + "', 0 " +
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
            Statement statement = DataWarehouseConnection.getConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT substr(url, instr(url, '?q=')+3) as word, url as url_full," +
                                                "replace( SUBSTR( substr(url, instr(url, '://')+3), 0,instr(substr(url, instr(url, '://')+3),'/')), 'www.', '') as url_domain  " +
                    "FROM t_ext_mozila_places " +
                    "where url like '%google.%' and url like '%?q=%'");


            insertWordInTable(rs, user);

        }catch (SQLException | ClassNotFoundException | ConnectionException e) {
            throw new ExtractionException(getModuleName(), "t_clean_words", "Error cleaning extracted - " + e.getMessage());
        }
    }

    private void transformEmailsTable(String user) {
        try {
            Statement statement = DataWarehouseConnection.getConnection().createStatement();

            PreparedStatement preparedStatement =  DataWarehouseConnection.getConnection().prepareStatement(
                    " INSERT INTO t_clean_emails (email, source_full, original_url, username_value, available_password, date, url_user_origin, url_browser_origin, table_origin) " +
                            " VALUES (?,?,?,?,?,?,?,?,?)");

            ResultSet rs = statement.executeQuery(
                    "SELECT  mp.url as url_full, " +
                                    "replace( SUBSTR( substr(mp.url, instr(mp.url, '://')+3), 0,instr(substr(mp.url, instr(mp.url, '://')+3),'/')), 'www.', '') as url_domain, " +
                                    "'TODO: path', title as url_title, typed as url_typed_count, " +
                                    "strftime('%Y-%m-%d %H:%M:%S', datetime(mh.visit_date/1000000, 'unixepoch', 'localtime')) as url_visit_time, " +
                                    "'" + user + "', '" + getModuleName() + "' " +
                            "FROM t_ext_mozila_places mp, t_ext_mozila_historyvisits mh " +
                            "WHERE mp.id = mh.place_id " +
                            "and url_domain <> ''; ");

            Pattern emailVerification = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");

            Matcher email;
            String encoded, substring, decode;

            while(rs.next()){
                encoded = rs.getString("url_full");
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
                    preparedStatement.setString(9, URL_ORIGIN.name());
                    preparedStatement.addBatch();
                }
            }
            preparedStatement.executeBatch();

        } catch (SQLException | ClassNotFoundException | ConnectionException e) {
            throw new ExtractionException(getModuleName(), "t_clean_emails", "Error cleaning extracted - " + e.getMessage());
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

    @Override
    public String getLoginDataFilename() {
        return "TODO: not implemented yet";
    }
}
