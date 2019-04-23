package main.pt.ipleiria.estg.dei.db;

import main.pt.ipleiria.estg.dei.db.etl.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.model.Login;
import main.pt.ipleiria.estg.dei.model.Website;
import main.pt.ipleiria.estg.dei.model.Word;
import main.pt.ipleiria.estg.dei.utils.Logger;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static main.pt.ipleiria.estg.dei.utils.Utils.atEndOfDay;
import static main.pt.ipleiria.estg.dei.utils.Utils.parse;

public class DatasetRepository {
    private static DatasetRepository datasetRepository;
    private Logger<DatasetRepository> logger = new Logger<>(DatasetRepository.class);
    private static Statement statement;

    private DatasetRepository() throws ConnectionException {
        try {
            statement = DataWarehouseConnection.getConnection().createStatement();
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Couldn't create statement - reason: " + e.getMessage());
            throw new ConnectionException("Couldn't create statement - reason: " + e.getMessage());
        }
    }


    public static DatasetRepository getInstance() throws ConnectionException, SQLException, ClassNotFoundException {
        if (datasetRepository == null) {
            datasetRepository = new DatasetRepository();
        }
        if (statement.isClosed()) {
            statement = DataWarehouseConnection.getConnection().createStatement();
        }
        return datasetRepository;
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
                        "INNER JOIN  t_clean_blocked_websites tcbw ON tcl.url_domain = tcbw.domain " +
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
                        "INNER JOIN  t_clean_blocked_websites tcbw ON tcl.url_domain = tcbw.domain " +
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
                "SELECT  email, source_full, count(*) as total " +
                        "FROM t_clean_emails " +
                        "group by email " +
                        "order by total desc ");

        while (rs.next()) {
            emails.add(new Login(rs.getString("email"),rs.getString("source_full"),
                    rs.getInt("total")));
        }
        return emails;
    }

    public List<Login> getLoginsUsed(String username) throws SQLException {
        List<Login> emails = new ArrayList<>();

        ResultSet rs = statement.executeQuery(
                "SELECT  email, source_full, count(*) as total " +
                        "FROM t_clean_emails " +
                        "WHERE url_user_origin = '" + username + "' " +
                        "group by email " +
                        "order by total desc ");

        while (rs.next()) {
            emails.add(new Login(rs.getString("email"),rs.getString("source_full"),
                    rs.getInt("total")));
        }
        return emails;
    }

    public List<Word> getWordsUsed() throws SQLException {
        List<Word> words = new ArrayList<>();
        ResultSet rs = statement.executeQuery(
                "SELECT  word, count(word) as times_used, url_user_origin, url_domain " +
                        " FROM t_clean_words " +
                        " group by word " +
                        " order by times_used desc ");

        while (rs.next()) {
            words.add(getWord(rs));
        }
        return words;
    }

    public List<Word> getWordsUsed(String username) throws SQLException {
        List<Word> words = new ArrayList<>();
        ResultSet rs = statement.executeQuery(
                "SELECT  word, count(word) as times_used, url_user_origin, url_domain " +
                        " FROM t_clean_words " +
                        "WHERE url_user_origin='" + username + "' " +
                        " group by word " +
                        " order by times_used desc ");

        while (rs.next()) {
            words.add(getWord(rs));
        }
        return words;
    }

    private Word getWord(ResultSet rs) throws SQLException {
        return new Word(rs.getString("word"),
                Integer.parseInt(rs.getString("times_used")),
                rs.getString("url_user_origin"),
                rs.getString("url_domain"));
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

    public int cleanAllData() throws SQLException, ConnectionException, ClassNotFoundException {
        int tableDeleted = 0;
        ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table'");
        Statement stmn = DataWarehouseConnection.getConnection().createStatement();
        while (rs.next()) {
            stmn.execute("DROP TABLE '"+ rs.getString("name") + "'");
            tableDeleted++;
        }
        logger.info("All previous data deleted.");
        return tableDeleted;
    }

    public void addToInfoExtract(String name) throws ConnectionException, SQLException, ClassNotFoundException {
        PreparedStatement preparedStatement = DataWarehouseConnection.getConnection().prepareStatement("INSERT INTO t_info_extract (name, last_extraction) VALUES (?, date('now'));");
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
                "select url_domain, url_user_origin, url_visit_time, url_full " +
                        "from t_clean_url " +
                        "where url_domain in ('"+ String.join("', '", domains)+"') " +
                        "and url_user_origin ='"+ username +"' " +
                        "ORDER BY url_domain asc, url_user_origin, url_visit_time desc");

        while (rs.next()) {
            websites.add(new Website(rs.getString("url_domain"), rs.getString("url_visit_time"),
                        rs.getString("url_full"), username));
        }
        return websites;
    }
    public List<Website> getActivityInWebsite(List<String> domains) throws SQLException {
        List<Website> websites = new ArrayList<>();
        ResultSet rs = statement.executeQuery(
                "select url_domain, url_user_origin, url_visit_time, url_full " +
                        "from t_clean_url " +
                        "where url_domain in ('"+ String.join("', '", domains)+"') " +
                        "ORDER BY url_domain asc, url_user_origin, url_visit_time desc");

        while (rs.next()) {
            websites.add(new Website(rs.getString("url_domain"), rs.getString("url_visit_time"),
                        rs.getString("url_full"), rs.getString("url_user_origin")));
        }
        return websites;
    }

    public List<Website> getVisitedWebsiteInDay(String username, Date date) throws SQLException {
        ResultSet rs = statement.executeQuery(
                "SELECT url_domain, url_user_origin,url_visit_time, sum(url_visit_duration)/(1000000) as totalTime " +
                "FROM t_clean_url " +
                "WHERE url_user_origin='"+ username+ "' " +
                "  and url_visit_time > '" + parse(date) +"' " +
                        "      AND url_visit_time < '" + atEndOfDay(date) +"'" +
                "GROUP BY url_domain, url_visit_time");

        return getWebsitesInADay(rs);
    }

    public List<Website> getVisitedWebsiteInDay(Date date) throws SQLException {
        ResultSet rs = statement.executeQuery(
                "SELECT url_domain, url_user_origin, url_visit_time, sum(url_visit_duration)/(60000000) as totalTime " +
                "FROM t_clean_url " +
                "WHERE url_visit_time > '" + parse(date) +" '" +
                "      AND url_visit_time < '" + atEndOfDay(date) +"'" +
                "GROUP BY url_domain, url_visit_time, url_user_origin ");
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
                start = dateFormat.parse(rs.getString("url_visit_time"));
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
            logger.warn("There was an issue when parsing a date: " + e.getMessage());
            return null;
        }
    }
}
