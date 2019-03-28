package main.pt.ipleiria.estg.dei.blocked;


import main.pt.ipleiria.estg.dei.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.utils.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

public enum  ISPLockedWebsites {
    INSTANCE;
    private Logger<ISPLockedWebsites> logger = new Logger<>(ISPLockedWebsites.class);

    ISPLockedWebsites() {
    }

    public HashMap readJsonFromUrl(String url)  {
        try{
            String jsonText = getLockedWebsites(url);
            JSONObject json = new JSONObject(jsonText);
            return parseFile(json);
        }catch ( IOException | JSONException e) {
            logger.error(e.getMessage());
            throw new ExtractionException(e.getMessage());
        }

    }

    public String getLockedWebsites(String urlToRead) throws IOException {
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

    public HashMap parseFile(JSONObject json) throws JSONException {
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
}
