package pt.ipleiria.estg.dei.db;

import pt.ipleiria.estg.dei.model.BrowserEnum;
import pt.ipleiria.estg.dei.model.GoogleChrome;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public enum GoogleChromeRepository {
    INSTANCE;

    GoogleChromeRepository(){
    }

    public  List<GoogleChrome> getMostVisitedSite(BrowserEnum browser) throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionFactory.getConnection(browser);
        Statement statement = connection.createStatement();
        List<GoogleChrome> histories = new ArrayList<>();
        ResultSet rs;
        rs = statement.executeQuery("SELECT  urls.url as url, count(*) as visit" +
                " FROM urls, visits " +
                " WHERE urls.id = visits.url " +
                " GROUP by urls.url " +
                " ORDER By visit_count DESC "
                + "LIMIT 5;");

        while (rs.next()) {
            String url = rs.getString("url");
            int visitNumber = Integer.parseInt(rs.getString("visit"));
            histories.add(new GoogleChrome(url, visitNumber));
        }
        return histories;
    }

}
