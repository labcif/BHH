package main.pt.ipleiria.estg.dei.db;

import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.model.Website;
import main.pt.ipleiria.estg.dei.utils.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatasetRepository {
    private static DatasetRepository datasetRepository = new DatasetRepository();
    private static Connection connection;
    private Logger<DatasetRepository> logger = new Logger<>(DatasetRepository.class);

    private DatasetRepository() {
        try {
            connection =ConnectionFactory.getConnection();
        } catch (ClassNotFoundException | SQLException e) {
            logger.error(e.getMessage());
            throw new ConnectionException("Could't connect to database. Reason: " + e.getMessage());
        }
    }


    public DatasetRepository getInstance() {
        return datasetRepository;
    }

    public static List<Website> getTopVisitedWebsite(int limit) throws SQLException {
        List<Website> website = new ArrayList<>();
        Statement statement = connection.createStatement();

        ResultSet rs = statement.executeQuery(
                "SELECT url_domain, SUM(url_visit_count) as total " +
                        "FROM t_clean_url " +
                        "group by url_domain " +
                        "order by SUM(url_visit_count) desc " +
                        "limit " + limit );

        while (rs.next()) {
            String domain = rs.getString("url_domain");
            int visitNumber = Integer.parseInt(rs.getString("total"));
            website.add(new Website(domain, visitNumber));
        }
        return website;
    }
}
