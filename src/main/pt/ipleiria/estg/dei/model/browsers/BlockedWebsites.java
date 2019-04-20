package main.pt.ipleiria.estg.dei.model.browsers;

import main.pt.ipleiria.estg.dei.db.etl.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.exceptions.TransformationException;
import main.pt.ipleiria.estg.dei.utils.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.sleuthkit.datamodel.Content;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class BlockedWebsites extends Module implements ETLProcess {
    Logger<BlockedWebsites> logger = new Logger<>(BlockedWebsites.class);
    private String chosenFile;

    public BlockedWebsites(String chosenFile) {
        this.chosenFile = chosenFile;
    }

    @Override
    public void run(Content dataSource) throws ConnectionException {
        logger.info("[" + getModuleName() +"] - Started");
        deleteExtractTables();
        extractAllTables();
        transformAllTables("");
        logger.info("[" + getModuleName() +"] - Finished");
    }

    @Override
    public void extractAllTables() throws ConnectionException {
        try {
            InputStream backup = getClass().getResourceAsStream("/resources/blocked_websites.csv");

            Set<String> urlSet = chosenFile != null ? parseCSVFile(new FileInputStream(chosenFile)).keySet() : parseCSVFile(backup).keySet();

            PreparedStatement preparedStatement = DataWarehouseConnection
                    .getConnection().prepareStatement("INSERT INTO main.t_ext_blocked_websites (domain) VALUES (?)");

            for (String url : urlSet) {
                preparedStatement.setString(1, url);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (SQLException | ClassNotFoundException | IOException  e) {
            throw new ExtractionException(getModuleName(), "t_ext_blocked_websites",e.getMessage());
        }
    }


    @Override
    public void transformAllTables(String user) throws ConnectionException {
        try {
            PreparedStatement preparedStatement = DataWarehouseConnection.getConnection().prepareStatement(
                " INSERT INTO t_clean_blocked_websites (domain) " +
                        " SELECT  domain " +
                        " FROM t_ext_blocked_websites ");

            preparedStatement.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            logger.error(e.getMessage());
            throw new TransformationException(getModuleName(),"t_clean_blocked_websites" ,"Error transforming tables: " + e.getMessage());
        }
    }

    @Override
    public void deleteExtractTables() throws ConnectionException {
        try {
            Statement stmt = DataWarehouseConnection.getConnection().createStatement();
            stmt.execute("DELETE FROM t_ext_blocked_websites;");
        } catch (SQLException | ClassNotFoundException e) {
            throw new ExtractionException(getModuleName(), "t_ext_blocked_websites" ,"Error deleting extract tables - " + e.getMessage());
        }

    }

    @Override
    public void deleteCleanTables() {
    }

    public HashMap readJsonFromUrl(String url) throws IOException, JSONException {
        String jsonText = getLockedWebsites(url);
        JSONObject json = new JSONObject(jsonText);
        return parseFile(json);
    }

    private String getLockedWebsites(String urlToRead) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try( BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))){
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }

    public HashMap parseCSVFile(InputStream locationFile) throws IOException {

        String line;
        HashMap blockedSites = new HashMap();
        String[] webSite;

        try(BufferedReader buffer = new BufferedReader(new InputStreamReader(locationFile))) {

            while ((line = buffer.readLine()) != null) {
                webSite = line.split(",");
                blockedSites.put(webSite[0],webSite[0]);
            }

        }
        return blockedSites;
    }

    private HashMap parseFile(JSONObject json) throws JSONException {
        HashMap blockedSites = new HashMap();
        JSONObject arr = json.getJSONObject("domains");
        Iterator<String> domainKeys = arr.keys();

        //domain
        while(domainKeys.hasNext()) {
            String keyDomain = domainKeys.next();
            if (arr.get(keyDomain) instanceof JSONObject) {
                //Host
                JSONObject domain =  arr.getJSONObject(keyDomain);
                JSONObject hosts =  domain.getJSONObject("hosts");

                if ( hosts.has("www")) {
                    JSONObject www =  hosts.getJSONObject("www");
                    JSONObject isps =  www.getJSONObject("isp");
                    Iterator<String> ispKeys = isps.keys();

                    while(ispKeys.hasNext()) {
                        String key = ispKeys.next();
                        if (isps.get(key) instanceof JSONObject) {
                            int status =  isps.getJSONObject(key).getInt("status");
                            if (status != 0 && blockedSites.get(keyDomain) == null){
                                blockedSites.put(keyDomain, status);
                            }
                        }
                    }
                }
            }
        }
        return blockedSites;
    }

    @Override
    public String getModuleName() {
        return "Blocked_Website";
    }

}
