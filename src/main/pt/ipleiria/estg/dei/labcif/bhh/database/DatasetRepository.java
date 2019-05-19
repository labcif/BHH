package main.pt.ipleiria.estg.dei.labcif.bhh.database;

import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.models.Login;
import main.pt.ipleiria.estg.dei.labcif.bhh.models.Website;
import main.pt.ipleiria.estg.dei.labcif.bhh.models.Word;
import main.pt.ipleiria.estg.dei.labcif.bhh.utils.LoggerBHH;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static main.pt.ipleiria.estg.dei.labcif.bhh.utils.Utils.atEndOfDay;
import static main.pt.ipleiria.estg.dei.labcif.bhh.utils.Utils.parse;

public class DatasetRepository {
    private LoggerBHH<DatasetRepository> loggerBHH = new LoggerBHH<>(DatasetRepository.class);
    private static Statement statement;
    private String databaseDirectory;

    public DatasetRepository(String databaseDirectory) throws ConnectionException {
        try {
            statement = DataWarehouseConnection.getConnection(databaseDirectory).createStatement();
            this.databaseDirectory = databaseDirectory;
        } catch (SQLException | ClassNotFoundException e) {
            loggerBHH.error("Couldn't create statement - reason: " + e.getMessage());
            throw new ConnectionException("Couldn't create statement - reason: " + e.getMessage());
        }
    }



    public List<Website> getMostVisitedWebsite(int limit) throws SQLException {
        List<Website> website = new ArrayList<>();

        ResultSet rs = statement.executeQuery(
                "SELECT url_domain, count (*) as total " +
                        "FROM t_clean_url " +
                        "group by url_domain " +
                        "order by count (*) desc " +
                        "limit " + limit);

        while (rs.next()) {
            website.add(getWebsites(rs));
        }
        return website;
    }
    public List<Website> getMostVisitedWebsite(int limit, String username) throws SQLException {
        List<Website> website = new ArrayList<>();

        ResultSet rs = statement.executeQuery(
                "SELECT url_domain, count (*) as total " +
                        "FROM t_clean_url " +
                        "WHERE url_user_origin = '" + username+"' " +
                        "group by url_domain " +
                        "order by count (*) desc " +
                        "limit " + limit);

        while (rs.next()) {
            website.add(getWebsites(rs));
        }
        return website;
    }



    private Website getWebsites(ResultSet rs) throws SQLException {
        String domain = rs.getString("url_domain");
        int visitNumber = Integer.parseInt(rs.getString("total"));
        return new Website(domain, visitNumber);
    }

    public List<Website> getBlockedVisitedWebsite() throws SQLException {
        List<Website> website = new ArrayList<>();

        ResultSet rs = statement.executeQuery(
                "SELECT url_domain, count (*) as total " +
                        "FROM t_clean_url tcl " +
                        "INNER JOIN  t_clean_special_websites tcbw ON tcl.url_domain = tcbw.special_websites_domain " +
                        "group by url_domain " +
                        "order by total desc ");

        while (rs.next()) {
            website.add(getWebsites(rs));
        }
        return website;
    }
    public List<Website> getBlockedVisitedWebsite(String username) throws SQLException {
        List<Website> website = new ArrayList<>();

        ResultSet rs = statement.executeQuery(
                "select url_domain, count(*) as total " +
                        "FROM t_clean_url tcl " +
                        "INNER JOIN  t_clean_special_websites tcbw ON tcl.url_domain = tcbw.special_websites_domain " +
                        "WHERE url_user_origin = '" + username +"' " +
                        "GROUP BY url_domain " +
                        "order by total desc " );

        while (rs.next()) {
            website.add(getWebsites(rs));
        }
        return website;
    }
    


    public List<Login> getLoginsUsed() throws SQLException {
        List<Login> emails = new ArrayList<>();

        ResultSet rs = statement.executeQuery(
                "SELECT  logins_email, logins_domain, count(*) as total " +
                        "FROM t_clean_logins " +
                        "group by email " +
                        "order by total desc ");

        while (rs.next()) {
            emails.add(new Login(rs.getString("logins_email"),rs.getString("logins_domain"),
                    rs.getInt("total")));
        }
        return emails;
    }

    public List<Login> getLoginsUsed(String username) throws SQLException {
        List<Login> emails = new ArrayList<>();

        ResultSet rs = statement.executeQuery(
                "SELECT  logins_email, logins_domain count(*) as total " +
                        "FROM t_clean_logins " +
                        "WHERE url_user_origin = '" + username + "' " +
                        "group by logins_email " +
                        "order by total desc ");

        while (rs.next()) {
            emails.add(new Login(rs.getString("logins_email"),rs.getString("logins_domain"),
                    rs.getInt("total")));
        }
        return emails;
    }

    public List<Word> getWordsUsed() throws SQLException {
        List<Word> words = new ArrayList<>();
        ResultSet rs = statement.executeQuery(
                "SELECT  search_in_engines_words, count(word) as times_used, search_in_engines_user_origin, search_in_engines_domain " +
                        " FROM t_clean_search_in_engines " +
                        " group by search_in_engines_words " +
                        " order by times_used desc ");

        while (rs.next()) {
            words.add(getWord(rs));
        }
        return words;
    }

    public List<Word> getWordsUsed(String username) throws SQLException {
        List<Word> words = new ArrayList<>();
        ResultSet rs = statement.executeQuery(
                "SELECT  search_in_engines_words, count(word) as times_used, search_in_engines_user_origin, search_in_engines_domain " +
                        " FROM t_clean_search_in_engines " +
                        "WHERE url_user_origin='" + username + "' " +
                        " group by search_in_engines_words " +
                        " order by times_used desc ");

        while (rs.next()) {
            words.add(getWord(rs));
        }
        return words;
    }

    private Word getWord(ResultSet rs) throws SQLException {
        return new Word(rs.getString("search_in_engines_words"),
                Integer.parseInt(rs.getString("times_used")),
                rs.getString("search_in_engines_user_origin"),
                rs.getString("search_in_engines_domain"));
    }

    public List<String> getUsers() throws SQLException {
        List<String> users = new ArrayList<>();

        ResultSet rs = statement.executeQuery(
                "select url_user_origin as user " +
                        "from t_clean_url " +
                        "group by url_user_origin;");

        while (rs.next()) {
            users.add(rs.getString("user"));
        }
        return users;
    }

    public boolean isFirstRunningImage(String imageName) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT name FROM t_info_extract WHERE name = '" + imageName + "';");
        return !rs.next();//It is the first time if there no results.
    }

    public void dropAllTables() throws SQLException, ConnectionException, ClassNotFoundException {
        int tableDeleted = 0;
        ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'");
        Statement stmn = DataWarehouseConnection.getConnection(databaseDirectory).createStatement();
        List<String> tablesToDelete = new ArrayList<>();
        while (rs.next()) {
            tablesToDelete.add(rs.getString("name"));
        }
        rs.close();
        for (String table : tablesToDelete) {
            stmn.execute("DROP TABLE '"+ table + "'");
            tableDeleted++;
        }

        loggerBHH.info(tableDeleted + " / " + tablesToDelete.size() +" tables deleted");
    }

    public void addToInfoExtract(String name) throws ConnectionException, SQLException, ClassNotFoundException {
        PreparedStatement preparedStatement = DataWarehouseConnection.getConnection(databaseDirectory).prepareStatement("INSERT INTO t_info_extract (name, last_extraction) VALUES (?, date('now'));");
        preparedStatement.setString(1, name);
        preparedStatement.executeUpdate();
    }

    public List<List<String>> execute(String query) throws SQLException {
        ResultSet rs = statement.executeQuery(query);
        ResultSetMetaData metaData = rs.getMetaData();
        int totalColumns = metaData.getColumnCount();
        List<List<String>> results = new ArrayList<>();
        while (rs.next()) {
            List<String> temp = new ArrayList<>();
            for (int i = 0; i < totalColumns; i++) {
                temp.add(rs.getString(i+1));
            }
            results.add(temp);
        }
        return results;
    }

    public List<Website> getActivityInWebsite(List<String> domains, String username) throws SQLException {
        List<Website> websites = new ArrayList<>();
        ResultSet rs = statement.executeQuery(
                "select url_domain, url_user_origin, url_visit_full_date_start, url_full " +
                        "from t_clean_url " +
                        "where url_domain in ('"+ String.join("', '", domains)+"') " +
                        "and url_user_origin ='"+ username +"' " +
                        "ORDER BY url_domain asc, url_user_origin, url_visit_full_date_start desc");

        while (rs.next()) {
            websites.add(new Website(rs.getString("url_domain"), rs.getString("url_visit_full_date_start"),
                        rs.getString("url_full"), username));
        }
        return websites;
    }
    public List<Website> getActivityInWebsite(List<String> domains) throws SQLException {
        List<Website> websites = new ArrayList<>();
        ResultSet rs = statement.executeQuery(
                "select url_domain, url_user_origin, url_visit_full_date_start, url_full " +
                        "from t_clean_url " +
                        "where url_domain in ('"+ String.join("', '", domains)+"') " +
                        "ORDER BY url_domain asc, url_user_origin, url_visit_full_date_start desc");

        while (rs.next()) {
            websites.add(new Website(rs.getString("url_domain"), rs.getString("url_visit_full_date_start"),
                        rs.getString("url_full"), rs.getString("url_user_origin")));
        }
        return websites;
    }

    public List<Website> getVisitedWebsiteInDay(String username, Date date) throws SQLException {
        ResultSet rs = statement.executeQuery(
                "SELECT url_domain, url_user_origin,url_visit_full_date_start, sum(url_visit_duration)/(1000000) as totalTime " +
                "FROM t_clean_url " +
                "WHERE url_user_origin='"+ username+ "' " +
                "  and url_visit_full_date_start > '" + parse(date) +"' " +
                        "      AND url_visit_full_date_start < '" + atEndOfDay(date) +"'" +
                "GROUP BY url_domain, url_visit_full_date_start");

        return getWebsitesInADay(rs);
    }

    public List<Website> getVisitedWebsiteInDay(Date date) throws SQLException {
        ResultSet rs = statement.executeQuery(
                "SELECT url_domain, url_user_origin, url_visit_full_date_start, sum(url_visit_duration)/(60000000) as totalTime " +
                "FROM t_clean_url " +
                "WHERE url_visit_full_date_start > '" + parse(date) +" '" +
                "      AND url_visit_full_date_start < '" + atEndOfDay(date) +"'" +
                "GROUP BY url_domain, url_visit_full_date_start, url_user_origin ");
        return getWebsitesInADay(rs);
    }

    private List<Website> getWebsitesInADay(ResultSet rs) throws SQLException {
        try {
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy  HH:mm:ss");
            List<Website> websites = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            long timeStart, timeEnd;
            Date start;
            String domain, user;
            int totalTime;
            while (rs.next()) {
                start = dateFormat.parse(rs.getString("url_visit_full_date_start"));
                timeStart = start.getTime();
                calendar.setTime(start);

                //For the sake of representation, unit min is 15 minutes.
                totalTime = rs.getInt("totalTime") < 15? 15: rs.getInt("totalTime");
                calendar.add(Calendar.MINUTE, totalTime);
                timeEnd = calendar.getTime().getTime();
                domain = rs.getString("url_domain");
                user = rs.getString("url_user_origin");
                websites.add(new Website(domain, user, new Timestamp(timeStart), new Timestamp(timeEnd)));
            }
            return websites;
        } catch (ParseException e) {
            loggerBHH.warn("There was an issue when parsing a date: " + e.getMessage());
            return null;
        }
    }
}
