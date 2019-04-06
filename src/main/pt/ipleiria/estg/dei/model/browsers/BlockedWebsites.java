package main.pt.ipleiria.estg.dei.model.browsers;

import main.pt.ipleiria.estg.dei.blocked.ISPLockedWebsites;
import main.pt.ipleiria.estg.dei.db.etl.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.exceptions.TransformationException;
import main.pt.ipleiria.estg.dei.utils.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

public class BlockedWebsites extends Data implements ETLProcess {
    Logger<BlockedWebsites> logger = new Logger<>(BlockedWebsites.class);

    @Override
    public void extractAllTables() {
        Set<String> urlSet = ISPLockedWebsites.INSTANCE.readJsonFromUrl("https://tofran.github.io/PortugalWebBlocking/blockList.json").keySet();
        try {
            PreparedStatement preparedStatement = DataWarehouseConnection.getConnection().prepareStatement("INSERT INTO main.t_ext_blocked_websites (domain) " +
                    " VALUES (?)");
            for (String url : urlSet) {
                preparedStatement.setString(1, url);
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
        } catch (SQLException e) {
            logger.error("Error extacting BlockedWebsites");
            throw new ExtractionException(e.getMessage());
        }


    }

    @Override
    public void runTransformation(String user) {
    }

    @Override
    public void transformAllTables(String user) {
        try {
            PreparedStatement preparedStatement = DataWarehouseConnection.getConnection().prepareStatement(
                " INSERT INTO t_clean_blocked_websites (domain) " +
                        " SELECT  domain " +
                        " FROM t_ext_blocked_websites ");

            preparedStatement.executeUpdate();
        } catch (SQLException  e) {
            logger.error(e.getMessage());
            throw new TransformationException("Error transforming tables: " + e.getMessage());
        }
    }

    @Override
    public void deleteExtractTables() {
        try {
            Statement stmt = DataWarehouseConnection.getConnection().createStatement();
            stmt.execute("DELETE FROM t_ext_blocked_websites;");
        } catch (SQLException e) {
            logger.error("Error deleting extract tables - " + e.getSQLState());
            throw new ExtractionException("Error deleting extract tables - " + e.getSQLState());
        }

    }

    @Override
    public void deleteCleanTables() {

    }
}
