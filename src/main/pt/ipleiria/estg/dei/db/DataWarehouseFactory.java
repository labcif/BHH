package main.pt.ipleiria.estg.dei.db;

import main.pt.ipleiria.estg.dei.exceptions.DatabaseInitializationException;
import main.pt.ipleiria.estg.dei.utils.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DataWarehouseFactory {
    private static DataWarehouseFactory dataWarehouseFactory;
    private static final String CONNECTION = "jdbc:sqlite:";//src/resources/database/
    private static final String DB_NAME = "/browser-history.db";
    private Logger<DataWarehouseFactory> logger = new Logger<>(DataWarehouseFactory.class);

    private DataWarehouseFactory(String databaseLocation) {
        try {
            setupDB(databaseLocation);
        } catch (ClassNotFoundException | SQLException | IOException e) {
            logger.error(e.getMessage());
            throw new DatabaseInitializationException(e.getMessage());
        }
    }

    public static void init(String databaseLocation) {
        dataWarehouseFactory = new DataWarehouseFactory(databaseLocation);
    }

    private void setupDB(String databaseLocation) throws ClassNotFoundException, SQLException, IOException {
        logger.info("Creating database...");
        createDB(databaseLocation);
        logger.info("Database created");

        logger.info("Start running migrations");
        runMigrations(databaseLocation);
        logger.info("All migration finished with success");
    }

    private void runMigrations(String databaseLocation) throws SQLException, IOException {
        for (String allMigration : getAllMigrations()) {
            executeMigration(allMigration, databaseLocation);
        }
    }

    private void executeMigration(String migration, String databaseLocation) throws IOException, SQLException {
        logger.info("Running migration: " + migration);
        try (BufferedReader br = new BufferedReader(new FileReader("src/resources/migrations/" + migration))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            Connection conn = DriverManager.getConnection(CONNECTION + databaseLocation + DB_NAME );
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
                if (sb.toString().contains(";")) {
                    String sqlStatement = sb.toString();
                    Statement stmt = conn.createStatement();
                    stmt.execute(sqlStatement);
                    sb = new StringBuilder();
                }
            }
            conn.close();
        }
        logger.info("Migration " + migration + " finished with success");
    }

    private List<String> getAllMigrations() {
        File folder = new File("src/resources/migrations");
        return Arrays.stream(Objects.requireNonNull(folder.listFiles())).map(File::getName).collect(Collectors.toList());
    }


    private void createDB(String databaseLocation) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        try (Connection conn = DriverManager.getConnection(CONNECTION + databaseLocation + DB_NAME)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                logger.info("The driver name is " + meta.getDriverName());
                logger.info("A new database has been created.");
            }
        }
    }

}
