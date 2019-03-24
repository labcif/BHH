package main.pt.ipleiria.estg.dei.db;


import main.pt.ipleiria.estg.dei.model.GoogleChrome;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class BroserHistoryDBArrangement {

    private static  String DBPATH = "jdbc:sqlite:" + System.getenv("SystemDrive") + "\\Users\\" + System.getenv("USERNAME")  +"\\Desktop\\browser-history";

    public static void createNewDatabase()  throws ClassNotFoundException, SQLException {

        Class.forName("org.sqlite.JDBC");
        Connection conection = DriverManager.getConnection(DBPATH);
        System.out.println("Opened database successfully");

        try(Statement stmt = conection.createStatement()){

            String sql = "CREATE TABLE IF NOT EXISTS DOMAIN_ACTIVITY " +
                    "(ID           INTEGER PRIMARY KEY     AUTOINCREMENT, " +
                    " DOMAIN       TEXT    NOT NULL, " +
                    " TITLE        TEXT    , " +
                    " VISIT_COUNT  INT     NOT NULL, " +
                    " DAILY_FREQUENCY      FLOAT, " +
                    " USER         TEXT    NOT NULL)";
            stmt.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS BLOKED_SITES " +
                    "(ID           INTEGER PRIMARY KEY     AUTOINCREMENT, " +
                    " DOMAIN       TEXT    NOT NULL)";
            stmt.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS WORDS_USE " +
                    "(ID           INTEGER PRIMARY KEY     AUTOINCREMENT, " +
                    " WORD         TEXT    NOT NULL, " +
                    " WORD_SOURCE  TEXT    NOT NULL, " + //So we can check contex if needed
                    " USER         TEXT    NOT NULL)";
            stmt.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS EMAIL " +
                    "(ID           INTEGER PRIMARY KEY     AUTOINCREMENT, " +
                    " EMAIL        TEXT    NOT NULL, " +
                    " EMAIL_SOURCE TEXT    , " + //So we can check contex if needed ?
                    " USER         TEXT    NOT NULL)";
            stmt.executeUpdate(sql);
        }
        conection.close();
    }

    public static void insertDomain(List<GoogleChrome> domainList) throws ClassNotFoundException, SQLException {

        Connection conection = DriverManager.getConnection(DBPATH);

        try(Statement stmt = conection.createStatement()) {
            Class.forName("org.sqlite.JDBC");
            conection.setAutoCommit(false);
            System.out.println("Opened database successfully");

            //exemp so no fancy for each
            for (GoogleChrome d :domainList) {
                String sql = "INSERT INTO DOMAIN_ACTIVITY (DOMAIN,TITLE,VISIT_COUNT,DAILY_FREQUENCY,USER) " +
                        "VALUES ('"+d.getUrl()+"', NULL , '"+d.getFromVisit()+"', NULL, 'Exemp user' );";
                stmt.executeUpdate(sql);
            }

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        conection.commit();
        conection.close();
        System.out.println("Records created successfully");
    }

}
