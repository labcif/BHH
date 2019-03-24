package main.pt.ipleiria.estg.dei.db.etl;

import main.pt.ipleiria.estg.dei.db.ConnectionFactory;
import main.pt.ipleiria.estg.dei.exceptions.TransformationException;
import main.pt.ipleiria.estg.dei.utils.Logger;

import java.sql.*;

public class Transformator {

    private Connection datawarehouseConnection;

    private Logger<Transformator> logger = new Logger<>(Transformator.class);

    private Transformator() {
        try {
            datawarehouseConnection = ConnectionFactory.getConnection();
            cleanTables();
            transformUrlTable();
        } catch (ClassNotFoundException | SQLException e) {
            logger.error(e.getMessage());
            throw new TransformationException(e.getMessage());
        }

    }

    private void transformUrlTable() throws SQLException {
        Statement statement = datawarehouseConnection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT  teu.url as url_full, replace( SUBSTR( substr(teu.url,instr(teu.url, '://')+3), 0,instr(substr(teu.url,instr(teu.url, '://')+3),'/')), 'www.', '') as url_domain " +
                                                " FROM t_ext_urls teu, t_ext_visits tev " +
                                                "WHERE teu.id = tev.url ");
        while (rs.next()) {
            PreparedStatement preparedStatement =
                    datawarehouseConnection.prepareStatement(
                            "INSERT INTO t_clean_url (url_full, url_domain, url_path, url_title, url_visit_count, " +
                                    "url_typed_count )" +
                            " VALUES(?, ?, ?, ?, ?, ?)");//TODO: falta acrescentar os restantes
            preparedStatement.setString(1, rs.getString("url_full"));
            preparedStatement.setString(2,  rs.getString("url_domain"));
            preparedStatement.setString(3, "TODO: path");
            preparedStatement.setString(4,"TODO: title");
            preparedStatement.setInt(5, 2);
            preparedStatement.setInt(6,  3);

            preparedStatement.executeUpdate();
        }
    }

    private void cleanTables() throws SQLException {
        Statement stmt = datawarehouseConnection.createStatement();
        stmt.execute("DELETE FROM t_clean_url;");
    }

    public static void tranform() {
        new Transformator();
    }


}
