package pt.ipleiria.estg.dei.blocked;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public enum ISPBlokedCSVReader {
    INSTANCE;

    ISPBlokedCSVReader() {
    }

    public HashMap readFile(String csvFile) {

        String line;

        HashMap blockedSites = new HashMap();
        String[] webSite;

        try(BufferedReader buffer = new BufferedReader(new FileReader(csvFile))) {

            while ((line = buffer.readLine()) != null) {

                webSite = line.split(",");

                blockedSites.put(webSite[0],webSite[0]);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return blockedSites;
    }
}
