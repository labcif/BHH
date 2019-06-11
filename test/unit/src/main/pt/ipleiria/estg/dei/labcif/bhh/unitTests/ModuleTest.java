package main.pt.ipleiria.estg.dei.labcif.bhh.unitTests;

import main.pt.ipleiria.estg.dei.labcif.bhh.database.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ConnectionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

class ModuleTest {
    static final String CORRECT_DIRECTORY = "test/unit/src/resources";
    private static final String T_EXT_CHROME_URLS = CORRECT_DIRECTORY + "/t_ext_chrome_urls.sql";

    void cleanDatabase() throws ConnectionException, SQLException, ClassNotFoundException {
        cleanChrome();
    }

    void fillDatabaseWithData() throws ConnectionException, SQLException, IOException, ClassNotFoundException {
        Statement stmt = DataWarehouseConnection.getConnection(CORRECT_DIRECTORY).createStatement();
        File file = new File(T_EXT_CHROME_URLS);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
                if (sb.toString().contains(";")) {
                    String sqlStatement = sb.toString();
                    stmt.execute(sqlStatement);
                    sb = new StringBuilder();
                }
            }
        }
        stmt.close();
    }

    private void cleanChrome() throws SQLException, ConnectionException, ClassNotFoundException {
        Statement stmt = DataWarehouseConnection.getConnection(CORRECT_DIRECTORY).createStatement();
        stmt.execute("DELETE FROM t_ext_chrome_urls;");
        stmt.execute("DELETE FROM t_ext_chrome_visits;");
        stmt.execute("DELETE FROM t_ext_chrome_visit_source;");
        stmt.execute("DELETE FROM t_ext_chrome_downloads;");
        stmt.execute("DELETE FROM t_ext_chrome_downloads_slices;");
        stmt.execute("DELETE FROM t_ext_chrome_downloads_url_chains;");
        stmt.execute("DELETE FROM t_ext_chrome_keyword_search_terms;");
        stmt.execute("DELETE FROM t_ext_chrome_segment_usage;");
        stmt.execute("DELETE FROM t_ext_chrome_segments;");
        stmt.execute("DELETE FROM t_ext_chrome_typed_url_sync_metadata;");
        stmt.execute("DELETE FROM t_ext_chrome_login_data;");
        stmt.close();
    }
}
