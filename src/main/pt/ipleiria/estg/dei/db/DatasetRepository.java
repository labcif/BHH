package main.pt.ipleiria.estg.dei.db;

import main.pt.ipleiria.estg.dei.db.etl.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.model.Email;
import main.pt.ipleiria.estg.dei.model.Website;
import main.pt.ipleiria.estg.dei.model.Word;
import main.pt.ipleiria.estg.dei.utils.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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


    public static DatasetRepository getInstance() throws ConnectionException {
        if (datasetRepository == null) {
            datasetRepository = new DatasetRepository();
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

    public List<Website> getTopVisitedWebsiteByUser(int limit, String userName) throws SQLException, ConnectionException, ClassNotFoundException {
        List<Website> website = new ArrayList<>();
        Statement statement =  DataWarehouseConnection.getConnection().createStatement();

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

    public List<Website> getBlockedWebsiteVisited() throws SQLException {
        List<Website> website = new ArrayList<>();

        ResultSet rs = statement.executeQuery(
                " SELECT url_domain, count(*) as total " +
                        " FROM t_clean_url " +
                        " WHERE url_domain IN (SELECT DOMAIN " +
                        "                       FROM t_clean_blocked_websites) " +
                        " group by url_domain " +
                        " order by total desc ");

        while (rs.next()) {
            website.add(new Website(rs.getString("url_domain"), Integer.parseInt(rs.getString("total"))));
        }
        return website;
    }

    public List<Email> getEmailsUsed() throws SQLException {
        List<Email> emails = new ArrayList<>();

        ResultSet rs = statement.executeQuery(
                "SELECT  email, source_full " +
                        "FROM t_clean_emails " +
                        "group by email ");

        while (rs.next()) {
            emails.add(new Email(rs.getString("email"),rs.getString("source_full") ));
        }
        return emails;
    }

    public List<Word> getWordsUsed() throws SQLException {
        List<Word> words = new ArrayList<>();

        ResultSet rs = statement.executeQuery(
                "SELECT  word, count(word) as times_used " +
                        "FROM t_clean_words " +
                        "group by word ");

        while (rs.next()) {
            words.add(new Word(rs.getString("word"), Integer.parseInt(rs.getString("times_used"))));
        }
        return words;
    }



}
