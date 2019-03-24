package main.pt.ipleiria.estg.dei.db;

import main.pt.ipleiria.estg.dei.exceptions.DatabaseInitializationException;
import main.pt.ipleiria.estg.dei.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataWarehouseFactory {
    private static DataWarehouseFactory dataWarehouseFactory;
    private static final String CONNECTION = "jdbc:sqlite:";//src/resources/database/
    private static final String DB_NAME = "/browser-history.db";
    private static final String MIGRATIONS_LOCATION = "/resources/migrations/";
    private Logger<DataWarehouseFactory> logger = new Logger<>(DataWarehouseFactory.class);

    private DataWarehouseFactory(String databaseLocation) {
        try {
            setupDB(databaseLocation);
        } catch (ClassNotFoundException | SQLException | IOException | URISyntaxException e) {
            logger.error(e.getMessage());
            throw new DatabaseInitializationException(e.getMessage());
        }
    }

    public static void init(String databaseLocation) {
        if (dataWarehouseFactory == null) {
            dataWarehouseFactory = new DataWarehouseFactory(databaseLocation);
        }
    }

    private void setupDB(String databaseLocation) throws ClassNotFoundException, SQLException, IOException, URISyntaxException {
        logger.info("Creating database...");
        createDB(databaseLocation);
        logger.info("Database created");

        logger.info("Start running migrations");
        runMigrations(databaseLocation);
        logger.info("All migration finished with success");
    }

    private void runMigrations(String databaseLocation) throws SQLException, IOException, URISyntaxException {
        for (String allMigration : getAllMigrations()) {
            executeMigration(allMigration, databaseLocation);
        }
    }

    private void executeMigration(String migration, String databaseLocation) throws IOException, SQLException, URISyntaxException {
        logger.info("Running migration: " + migration);

        InputStream resourceAsStream = getClass().getResourceAsStream(migration);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8))) {
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

    private List<String> getAllMigrations() throws URISyntaxException, IOException {
        URI uri = getClass().getResource(MIGRATIONS_LOCATION).toURI();
        Path myPath;
        if (uri.getScheme().equals("jar")) {
            FileSystem fileSystem;
            try {
                fileSystem = FileSystems.getFileSystem(uri);
            } catch (Exception ex) {
                 fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
            }

            myPath = fileSystem.getPath(MIGRATIONS_LOCATION);
        } else {
            myPath = Paths.get(uri);
        }
        Stream<Path> migrations = Files.walk(myPath, 1);

        return migrations.map(migration -> MIGRATIONS_LOCATION + migration.getFileName())
                .filter(migration-> migration.endsWith(".sql"))
                .collect(Collectors.toList());
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
