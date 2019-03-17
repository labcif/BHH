package pt.ipleiria.estg.dei.db;

import pt.ipleiria.estg.dei.blocked.ISPLockedWebsites;
import pt.ipleiria.estg.dei.model.GoogleChrome;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static pt.ipleiria.estg.dei.model.BrowserEnum.CHROME;

public enum GoogleChromeRepository {
    INSTANCE;

    GoogleChromeRepository(){
    }

    public  List<GoogleChrome> getMostVisitedSite() throws SQLException, ClassNotFoundException {
        Connection connection = ConnectionFactory.getConnection(CHROME);
        ResultSet rs;
        Statement statement = connection.createStatement();
        List<GoogleChrome> histories = new ArrayList<>();
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

    public  List<GoogleChrome> getBlockedSitesVisited() throws Exception {
        Connection connection = ConnectionFactory.getConnection(CHROME);
        ResultSet rs;
        Statement statement = connection.createStatement();
        List<GoogleChrome> histories = new ArrayList<>();
        rs = statement.executeQuery(" SELECT replace( SUBSTR( substr(url,instr(url, '://')+3), 0,instr(substr(url,instr(url, '://')+3),'/')), 'www.', '') as urlDomain, sum(visit_count) as visitDomain " +
                " FROM urls " +
                " GROUP BY urlDomain " +
                " order by visitDomain desc ");

        HashMap blockedWebisites = ISPLockedWebsites.INSTANCE.readJsonFromUrl("https://tofran.github.io/PortugalWebBlocking/blockList.json");

        while (rs.next()) {

            String url = rs.getString("urlDomain");

            if(blockedWebisites.containsKey(url)){
                int visitNumber = Integer.parseInt(rs.getString("visitDomain"));
                histories.add(new GoogleChrome(url, visitNumber));
            }
        }
        return histories;
    }

    public List<String> getWordsFromGoogleEngine() throws SQLException, ClassNotFoundException, UnsupportedEncodingException {
        Connection connection = ConnectionFactory.getConnection(CHROME);
        ResultSet rs;
        Statement statement = connection.createStatement();
        List<String> histories = new ArrayList<>();
        rs = statement.executeQuery("SELECT substr(url, instr(url, '?q=')+3) as word  " +
                "FROM urls " +
                "where url like '%google.%' and url like '%?q=%'");

        while (rs.next()) {
            String encoded = rs.getString("word");
            String substring = encoded.substring(0, encoded.indexOf("&"));
            String decode = URLDecoder.decode(substring, "UTF-8");
            histories.add(decode);
        }
        return histories;
    }

}
