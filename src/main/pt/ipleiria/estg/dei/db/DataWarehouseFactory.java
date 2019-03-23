package main.pt.ipleiria.estg.dei.db;

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
    private static DataWarehouseFactory dataWarehouseFactory = new DataWarehouseFactory();
    private static final String CONNECTION = "jdbc:sqlite:src/resources/database/browser-history.db";

    private DataWarehouseFactory() {
        try {
            setupDB();
        } catch (ClassNotFoundException | SQLException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void setupDB() throws ClassNotFoundException, SQLException, IOException {
        createDB();
        System.out.println("Start running migrations");
        runMigrations();
        System.out.println("All migration finished with success");
    }

    private void runMigrations() throws SQLException, IOException {
        List<String> allMigrations = getAllMigrations();

        for (String allMigration : allMigrations) {
            executeMigration(allMigration);
        }

    }

    private void executeMigration(String migration) throws IOException, SQLException {
        System.out.println("Running migration: " + migration);
        try (BufferedReader br = new BufferedReader(new FileReader("src/resources/migrations/" + migration))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            Connection conn = DriverManager.getConnection(CONNECTION );
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



            System.out.println("Migration " + migration + " finished with success");
        }
    }

    private List<String> getAllMigrations() {
        File folder = new File("src/resources/migrations");
        return Arrays.stream(Objects.requireNonNull(folder.listFiles())).map(File::getName).collect(Collectors.toList());
    }


    private void createDB() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        try (Connection conn = DriverManager.getConnection(CONNECTION)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println();
    }


    public static DataWarehouseFactory getInstance() {
        return dataWarehouseFactory;
    }
}
