package main.pt.ipleiria.estg.dei.labcif.bhh.database;



import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.DatabaseInitializationException;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.MigrationException;
import main.pt.ipleiria.estg.dei.labcif.bhh.utils.LoggerBHH;

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
    private static final String MIGRATIONS_LOCATION = "/resources/migrations/";

    private LoggerBHH<DatabaseCreator> loggerBHH = new LoggerBHH<>(DatabaseCreator.class);
    private String databaseDirectory;

    private DatabaseCreator(String databaseDirectory) throws MigrationException, DatabaseInitializationException, ConnectionException {
        this.databaseDirectory = databaseDirectory;
        setupDB();
    }
    public static void init(String databaseDirectory) throws MigrationException, DatabaseInitializationException, ConnectionException {
        new DatabaseCreator(databaseDirectory);
    }

    private void setupDB() throws MigrationException, DatabaseInitializationException, ConnectionException {
        loggerBHH.info("Preparing database...");
        createDB();
        runMigrations();
        loggerBHH.info("Database prepared...");
    }

    private void runMigrations() throws MigrationException, ConnectionException {
        try {
            for (String allMigration : getAllMigrations()) {
                executeMigration(allMigration);
            }
        } catch (URISyntaxException | IOException | SQLException | ClassNotFoundException e) {
            throw new MigrationException(e.getMessage());
        }
    }

    private void executeMigration(String migration) throws IOException, SQLException, ClassNotFoundException, ConnectionException {
        loggerBHH.info("Running migration: " + migration);

        InputStream resourceAsStream = getClass().getResourceAsStream(MIGRATIONS_LOCATION + migration);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            Connection conn = DataWarehouseConnection.getConnection(databaseDirectory);
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
        }
        loggerBHH.info("Migration " + migration + " finished with success");
        registerInSchema(migration);

    }

    private void registerInSchema(String fullname) throws SQLException, ClassNotFoundException, ConnectionException {
        String version = fullname.split("__")[0];
        PreparedStatement preparedStatement = DataWarehouseConnection.getConnection(databaseDirectory).prepareStatement("INSERT INTO schema_version (version, name) VALUES (?, ?);");
        preparedStatement.setString(1, version);
        preparedStatement.setString(2, fullname);
        preparedStatement.executeUpdate();

    }

    private List<String> getMigrationsRegisterInSchema() throws SQLException, ClassNotFoundException, ConnectionException {
        Statement statement = DataWarehouseConnection.getConnection(databaseDirectory).createStatement();
        List<String> names = new ArrayList<>();
        ResultSet rs = statement.executeQuery("SELECT name from schema_version; ");
        while (rs.next()) {
            names.add(rs.getString("name"));
        }
        return names;
    }

    private List<String> getAllMigrations() throws URISyntaxException, IOException, SQLException, ClassNotFoundException, ConnectionException {
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


    private void createDB() throws DatabaseInitializationException {
        try {
            Connection conn = DataWarehouseConnection.getConnection(databaseDirectory);
            Statement stmt = conn.createStatement();

            stmt.execute("create table if not exists schema_version " +
                    "( " +
                    "  id INTEGER " +
                    "    constraint schema_version_pk " +
                    "      primary key autoincrement, " +
                    "  version FLOAT, " +
                    "  name VARCHAR(255) not null " +
                    "); ");
            stmt.execute("create unique index if not exists schema_version_NAME_uindex " +
                    "  on schema_version (name); ");
            stmt.execute("create unique index if not exists schema_version_version_uindex " +
                    "  on schema_version (version); ");

        } catch (ClassNotFoundException | SQLException | ConnectionException e) {
            throw new DatabaseInitializationException(e.getMessage());
        }

    }
}
