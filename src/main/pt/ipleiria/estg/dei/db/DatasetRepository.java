package main.pt.ipleiria.estg.dei.db;

import main.pt.ipleiria.estg.dei.db.etl.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.model.Website;
import main.pt.ipleiria.estg.dei.model.Word;
import main.pt.ipleiria.estg.dei.utils.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatasetRepository {
    private static DatasetRepository datasetRepository = new DatasetRepository();
    private Logger<DatasetRepository> logger = new Logger<>(DatasetRepository.class);

    private DatasetRepository() {

    }


    public DatasetRepository getInstance() {
        return datasetRepository;
    }

    public static List<Website> getTopVisitedWebsite(int limit) throws SQLException {
        List<Website> website = new ArrayList<>();
        Statement statement =  DataWarehouseConnection.getDatawarehouseConnection().createStatement();

        ResultSet rs = statement.executeQuery(
                "SELECT url_domain, SUM(url_visit_count) as total " +
                        "FROM t_clean_url " +
                        "group by url_domain " +
                        "order by SUM(url_visit_count) desc " +
                        "limit " + limit);

        while (rs.next()) {
            String domain = rs.getString("url_domain");
            int visitNumber = Integer.parseInt(rs.getString("total"));
            website.add(new Website(domain, visitNumber));
        }
        return website;
    }

    public static List<Website> getBlockedWebsiteVisited() throws SQLException {
        List<Website> website = new ArrayList<>();
        Statement statement =  DataWarehouseConnection.getDatawarehouseConnection().createStatement();

        ResultSet rs = statement.executeQuery(
                "SELECT url_domain, SUM(url_visit_count) as total " +
                        "FROM t_clean_url " +
                        " WHERE url_domain IN (SELECT DOMAIN " +
                        "                   FROM t_clean_blocked_websites) " +
                        "group by url_domain " +
                        "order by SUM(url_visit_count) desc ");

        while (rs.next()) {
            website.add(new Website(rs.getString("url_domain"), Integer.parseInt(rs.getString("total"))));
        }
        return website;
    }

    public static List<String> getEmailsUsed() throws SQLException {
        List<String> emails = new ArrayList<>();
        Statement statement =  DataWarehouseConnection.getDatawarehouseConnection().createStatement();

        ResultSet rs = statement.executeQuery(
                "SELECT  * " +
                        "FROM t_clean_emails " +
                        "group by email ");

        while (rs.next()) {
            emails.add(rs.getString("email"));
        }
        return emails;
    }

    public static List<Word> getWordsUsed() throws SQLException {
        List<Word> words = new ArrayList<>();
        Statement statement =  DataWarehouseConnection.getDatawarehouseConnection().createStatement();

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
