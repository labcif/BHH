package pt.ipleiria.estg.dei.blocked;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

public enum  ISPLockedWebsites {
    INSTANCE;

    ISPLockedWebsites() {
    }

    public HashMap readJsonFromUrl(String url) throws Exception {
        String jsonText = getLockedWebsites(url);
        JSONObject json = new JSONObject(jsonText);
        return parseFile(json);
    }

    public String getLockedWebsites(String urlToRead) throws Exception {
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

    public HashMap parseFile(JSONObject json) throws Exception{
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
