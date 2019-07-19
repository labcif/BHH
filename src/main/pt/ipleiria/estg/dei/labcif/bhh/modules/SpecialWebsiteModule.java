package main.pt.ipleiria.estg.dei.labcif.bhh.modules;

import main.pt.ipleiria.estg.dei.labcif.bhh.database.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.TransformationException;
import main.pt.ipleiria.estg.dei.labcif.bhh.models.OperatingSystem;
import main.pt.ipleiria.estg.dei.labcif.bhh.utils.LoggerBHH;
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

public class SpecialWebsiteModule extends Module implements ETLProcess {
    LoggerBHH<SpecialWebsiteModule> loggerBHH = new LoggerBHH<>(SpecialWebsiteModule.class);
    private String chosenFile;

    public SpecialWebsiteModule(String chosenFile, String databaseDirectory) {
        super(databaseDirectory);
        this.chosenFile = chosenFile;
    }

    public SpecialWebsiteModule(String databaseDirectory) {
        super(databaseDirectory);
    }

    //TODO: review this if time
    @Override
    public void run(Content dataSource) throws ConnectionException {
        run();
    }

    @Override
    public void run(String caseDirectory) throws ConnectionException {
        run();
    }

    private void run() throws ConnectionException {
        loggerBHH.info("[" + getModuleName() +"] - Started");
        deleteExtractTables();
        extractAllTables();
        transformAllTables("", "", "", null);
        loggerBHH.info("[" + getModuleName() +"] - Finished");
    }

    @Override
    public void extractAllTables() throws ConnectionException {
        try {
            InputStream backup = getClass().getResourceAsStream("/resources/blocked_websites.csv");

            Set<String> urlSet = chosenFile != null ? parseCSVFile(new FileInputStream(chosenFile)).keySet() : parseCSVFile(backup).keySet();

            PreparedStatement preparedStatement = DataWarehouseConnection
                    .getConnection(databaseDirectory).prepareStatement("INSERT INTO main.t_ext_blocked_websites (domain) VALUES (?)");

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
    public void transformAllTables(String user, String profileName, String fullLocationFile, OperatingSystem os) throws ConnectionException {
        try {
            PreparedStatement preparedStatement = DataWarehouseConnection.getConnection(databaseDirectory).prepareStatement(
                " INSERT INTO t_clean_special_websites (special_websites_domain) " +
                        " SELECT  domain " +
                        " FROM t_ext_blocked_websites ");

            preparedStatement.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            loggerBHH.error(e.getMessage());
            throw new TransformationException(getModuleName(),"t_clean_special_websites" ,"Error transforming tables: " + e.getMessage());
        }
    }

    @Override
    public void deleteExtractTables() throws ConnectionException {
        try {
            Statement stmt = DataWarehouseConnection.getConnection(databaseDirectory).createStatement();
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
        HashMap<String,String > blockedSites = new HashMap();
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
        HashMap<String, Integer> blockedSites = new HashMap();
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
