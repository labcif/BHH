package main.pt.ipleiria.estg.dei.labcif.bhh.unitTests;

import main.pt.ipleiria.estg.dei.labcif.bhh.database.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ConnectionException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.*;


public class DataWarehouseConnectionTest {
    private String databaseLocation;


    @Before
    public void setUp() {
        File file = new File("src/resources/");
        databaseLocation = file.getAbsolutePath();
    }


    @Test
    public void get_connection_successfully() {
        Connection connection = null;
        try {
            connection = DataWarehouseConnection.getConnection(databaseLocation);
            assertNotNull(connection);
        } catch (SQLException | ClassNotFoundException | ConnectionException e) {
            fail();
        }
        finally {
            DataWarehouseConnection.closeConnection();
        }
    }

    @Test
    public void get_only_one_connection () {
        Connection connection1 = null;
        Connection connection2 = null;
        try {
            connection1 = DataWarehouseConnection.getConnection(databaseLocation);
            connection2 = DataWarehouseConnection.getConnection(databaseLocation);
            assertNotNull(connection1);
            assertNotNull(connection2);
            assertEquals(connection1, connection2);
        }catch (SQLException | ClassNotFoundException | ConnectionException e) {
            fail();
        } finally {
            DataWarehouseConnection.closeConnection();
        }
    }

    @Test
    public void connection_is_closed_successfully () throws SQLException {
        Connection connection = null;
        try {
            connection = DataWarehouseConnection.getConnection(databaseLocation);
            assertNotNull(connection);
        } catch (SQLException | ClassNotFoundException | ConnectionException e) {
            fail();
        }finally {
            DataWarehouseConnection.closeConnection();
            assertNotNull(connection);
            assertTrue(connection.isClosed());
        }
    }

    @Test
    public void after_closing_connection_I_get_different_one () throws SQLException {
        Connection connection1 = null;
        Connection connection2 = null;
        try {
            connection1 = DataWarehouseConnection.getConnection(databaseLocation);
            assertNotNull(connection1);
            DataWarehouseConnection.closeConnection();
            assertTrue(connection1.isClosed());

            connection2 = DataWarehouseConnection.getConnection(databaseLocation);
            assertNotNull(connection2);

            assertNotSame(connection1, connection2);
        }catch (SQLException | ClassNotFoundException | ConnectionException e) {
            fail();
        } finally {
            DataWarehouseConnection.closeConnection();
            assertNotNull(connection2);
            assertTrue(connection2.isClosed());
        }
    }
}
