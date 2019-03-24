package main.pt.ipleiria.estg.dei.db.etl;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DatabaseCreator {
    private static final String CONNECTION = "jdbc:sqlite:";//src/resources/database/
    private static final String DB_NAME = "/browser-history.db";
    private static final String MIGRATIONS_LOCATION = "/resources/migrations/";
    private static String databaseLocation;

    private Logger<DatabaseCreator> logger = new Logger<>(DatabaseCreator.class);
    public static String FULL_PATH_CONNECTION;

    private DatabaseCreator(String databaseLocation) {
        try {
            DatabaseCreator.databaseLocation = databaseLocation;
            FULL_PATH_CONNECTION = CONNECTION + databaseLocation + DB_NAME;
            setupDB();
        } catch (ClassNotFoundException | SQLException | IOException | URISyntaxException e) {
            e.printStackTrace();
        }

    }
    public static void init(String databaseLocation){
        new DatabaseCreator(databaseLocation);
    }

    private void setupDB() throws ClassNotFoundException, SQLException, IOException, URISyntaxException {
        logger.info("Creating database...");
        createDB();
        logger.info("Database created");

        logger.info("Start running migrations");
        runMigrations();
        logger.info("All migration finished with success");
    }

    private void runMigrations() throws SQLException, IOException, URISyntaxException {
        for (String allMigration : getAllMigrations()) {
            executeMigration(allMigration);
        }
    }

    private void executeMigration(String migration) throws IOException, SQLException {
        logger.info("Running migration: " + migration);

        InputStream resourceAsStream = getClass().getResourceAsStream(MIGRATIONS_LOCATION +migration);
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
        registerInSchema(migration);

    }

    private void registerInSchema(String fullname) throws SQLException {
        String version = fullname.split("__")[0];
        try (Connection conn = DriverManager.getConnection(CONNECTION + databaseLocation + DB_NAME)) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO schema_version (version, name) VALUES (?, ?);");
            preparedStatement.setString(1, version);
            preparedStatement.setString(2, fullname);
            preparedStatement.executeUpdate();
        }
    }

    private List<String> getMigrationsRegisterInSchema() throws SQLException {
        try (Connection conn = DriverManager.getConnection(CONNECTION + databaseLocation + DB_NAME)) {
            Statement statement = conn.createStatement();
            List<String> names = new ArrayList<>();
            ResultSet rs = statement.executeQuery("SELECT name from schema_version; ");
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
            return names;
        }
    }

    private List<String> getAllMigrations() throws URISyntaxException, IOException, SQLException {
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
        List<String> migrationsRegisterInSchema = getMigrationsRegisterInSchema();
        List<String> newMigrations = migrations
                .map(migration -> migration.getFileName().toString())
                .filter(migration -> migration.endsWith(".sql"))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
        newMigrations.removeIf(migrationsRegisterInSchema::contains);
        return newMigrations;
    }


    private void createDB() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        try (Connection conn = DriverManager.getConnection(CONNECTION + databaseLocation + DB_NAME)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                logger.info("The driver name is " + meta.getDriverName());
                logger.info("A new database has been created.");
                Statement stmt = conn.createStatement();

                logger.info("Creating version table...");
                stmt.execute("create table if not exists schema_version " +
                        "( " +
                        "  id INTEGER " +
                        "    constraint schema_version_pk " +
                        "      primary key autoincrement, " +
                        "  version FLOAT, " +
                        "  name VARCHAR2(255) not null " +
                        "); ");
                stmt.execute("create unique index if not exists schema_version_NAME_uindex " +
                        "  on schema_version (name); ");
                stmt.execute("create unique index if not exists schema_version_version_uindex " +
                        "  on schema_version (version); ");
                logger.info("Version table created");
            }
        }
    }
}
