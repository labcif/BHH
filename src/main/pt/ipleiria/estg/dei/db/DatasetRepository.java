package main.pt.ipleiria.estg.dei.db;

import main.pt.ipleiria.estg.dei.db.etl.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.model.Login;
import main.pt.ipleiria.estg.dei.model.Website;
import main.pt.ipleiria.estg.dei.model.Word;
import main.pt.ipleiria.estg.dei.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public List<Website> getTopVisitedWebsite(int limit) throws SQLException {
        List<Website> website = new ArrayList<>();

        ResultSet rs = statement.executeQuery(
                "SELECT url_domain, count (*) as total " +
                        "FROM t_clean_url " +
                        "group by url_domain " +
                        "order by count (*) desc " +
                        "limit " + limit);

        while (rs.next()) {
            String domain = rs.getString("url_domain");
            int visitNumber = Integer.parseInt(rs.getString("total"));
            website.add(new Website(domain, visitNumber));
        }
        return website;
    }

    public List<Website> getTopVisitedWebsiteByUser(int limit, String userName) throws SQLException {
        List<Website> website = new ArrayList<>();

        ResultSet rs = statement.executeQuery(
                "SELECT url_domain, count (*) as total " +
                        "FROM t_clean_url " +
                        "where url_user_origin  = '" + userName + "'"   +
                        "group by url_domain " +
                        "order by count (*) desc " +
                        "limit " + limit);

        while (rs.next()) {
            String domain = rs.getString("url_domain");
            int visitNumber = Integer.parseInt(rs.getString("total"));
            website.add(new Website(domain, visitNumber));
        }
        return website;
    }

    public List<Website> getBlockedWebsiteVisited(int limit) throws SQLException {
        List<Website> website = new ArrayList<>();

        ResultSet rs = statement.executeQuery(
                " SELECT url_domain, count(*) as total " +
                        " FROM t_clean_url " +
                        " WHERE url_domain IN (SELECT DOMAIN " +
                        "                       FROM t_clean_blocked_websites) " +
                        " group by url_domain " +
                        " order by total desc " +
                        "limit " +limit );

        while (rs.next()) {
            website.add(new Website(rs.getString("url_domain"), Integer.parseInt(rs.getString("total"))));
        }
        return website;
    }
    
    public List<Website> getBlockedWebsiteVisited(int limit, String userName) throws SQLException {
        List<Website> website = new ArrayList<>();

        ResultSet rs = statement.executeQuery(
                " SELECT url_domain, count(*) as total " +
                        " FROM t_clean_url " +
                        " WHERE url_domain IN (SELECT DOMAIN " +
                        "                       FROM t_clean_blocked_websites) " +
                        " and url_user_origin  = '" + userName + "'"   +
                        " group by url_domain " +
                        " order by total desc " +
                        " limit " + limit);

        while (rs.next()) {
            website.add(new Website(rs.getString("url_domain"), Integer.parseInt(rs.getString("total"))));
        }
        return website;
    }

    public List<Login> getEmailsUsed() throws SQLException {
        List<Login> emails = new ArrayList<>();

        ResultSet rs = statement.executeQuery(
                "SELECT  email, source_full, count(*) as total, available_password " +
                        "FROM t_clean_emails " +
                        "group by email " +
                        "order by total desc ");

        while (rs.next()) {
            emails.add(new Login(rs.getString("email"),rs.getString("source_full"),
                    rs.getInt("total"), rs.getString("available_password")));
        }
        return emails;
    }

    public List<Word> getWordsUsed() throws SQLException {
        List<Word> words = new ArrayList<>();
        ResultSet rs = statement.executeQuery(
                "SELECT  word, count(word) as times_used " +
                        " FROM t_clean_words " +
                        " group by word " +
                        " order by times_used desc " +
                        " limit 10 ");

        while (rs.next()) {
            words.add(new Word(rs.getString("word"), Integer.parseInt(rs.getString("times_used"))));
        }

        return words;
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
            stmn.execute("DELETE FROM '"+ rs.getString("name") + "'");
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
}
