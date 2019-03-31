package main.pt.ipleiria.estg.dei.model.browsers;

public interface ETLProcess {

    void runExtraction(String path);
    void extractTable(String newTable, String oldTable, String externalDB);
    void extractAllTables();

    void runTransformation(String user);
    void transformAllTables(String user);

    void deleteExtractTables();
    void deleteCleanTables();

    void runETLProcess(String path, String user);
}
