package main.pt.ipleiria.estg.dei.labcif.bhh.modules;

import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ConnectionException;
import org.sleuthkit.datamodel.Content;

public interface ETLProcess {
    void run(Content dataSource) throws ConnectionException;
    void run(String caseDirectory) throws ConnectionException;

    void runExtraction(String path) throws ConnectionException;
    void extractTable(String newTable, String oldTable, String externalDB) throws ConnectionException;
    void extractAllTables() throws ConnectionException;

    void runTransformation(String user, String profileName, String fullLocationFile) throws ConnectionException;
    void transformAllTables(String user, String profileName, String fullLocationFile) throws ConnectionException;

    void deleteExtractTables() throws ConnectionException;
    void deleteCleanTables() throws ConnectionException;

    void runETLProcess(String databaseOriginFullPath, String user, String profileName, String fullLocationFile) throws ConnectionException;
}
