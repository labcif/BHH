package main.pt.ipleiria.estg.dei.labcif.bhh.unitTests;


import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.modules.ChromeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GoogleChromeModuleTest extends ModuleTest{
    private ChromeModule chromeModuleInvalid;
    private ChromeModule chromeModuleValid;

    private static final String INCORRECT_DIRECTORY = "directory/something";
    private static final String USER = "user#1";
    private static final String PROFILE = "Default";

    @BeforeAll
    void setUp() throws ConnectionException, SQLException, IOException, ClassNotFoundException {
        chromeModuleInvalid = new ChromeModule(INCORRECT_DIRECTORY);
        chromeModuleValid = new ChromeModule(CORRECT_DIRECTORY);
        cleanDatabase();
        fillDatabaseWithData();

    }

    @Test
    void check_history_filename() {
        assertEquals(chromeModuleInvalid.getHistoryFilename(), "History");
        assertEquals(chromeModuleValid.getHistoryFilename(), "History");
    }

    @Test
    void check_login_filename_correct() {
        assertEquals(chromeModuleInvalid.getLoginDataFilename(), "Login Data");
        assertEquals(chromeModuleValid.getLoginDataFilename(), "Login Data");
    }

    @Test
    void check_module_name_correct() {
        assertEquals(chromeModuleInvalid.getModuleName(), "GOOGLE_CHROME");
        assertEquals(chromeModuleValid.getModuleName(), "GOOGLE_CHROME");
    }

    @Test
    void check_path_to_hitory_filename_windows() {
        assertEquals(chromeModuleInvalid.getPathToBrowserInstallation(), "AppData/Local/Google/Chrome/User Data/Default");
        assertEquals(chromeModuleValid.getPathToBrowserInstallation(), "AppData/Local/Google/Chrome/User Data/Default");
    }
    @Test
    void check_path_to_hitory_filename_linux() {
        fail("TODO: Method not implemented yet");
    }

    @Test
    void extract_invalid_table_fail()  {
        assertThrows(ExtractionException.class, ()-> chromeModuleInvalid.extractAllTables());
    }

    @Test
    void extract_valid_tables_fail()  {
        assertThrows(ExtractionException.class, ()-> chromeModuleValid.extractAllTables());
    }


    @Test
    void tansforrm_valid_table_fail() {
        assertThrows(ExtractionException.class, () -> chromeModuleValid.transformAllTables(USER, PROFILE, "asda"));
    }
    @Test
    void extract_all_tables_success()  {

    }

    @Test
    void original_directory_is_saved_correctly() throws ConnectionException, SQLException {
        chromeModuleValid.run(CORRECT_DIRECTORY);
        Statement statement = DriverManager.getConnection(CORRECT_DIRECTORY).createStatement();
        ResultSet rs = statement.executeQuery(
                "SELECT url_filename_location " +
                        "FROM t_clean_url ");

        while (rs.next()) {
            String location = rs.getString("url_filename_location");
            assertEquals(location, CORRECT_DIRECTORY);
        }
    }
}
