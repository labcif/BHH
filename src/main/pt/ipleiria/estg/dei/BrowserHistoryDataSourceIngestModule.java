
package main.pt.ipleiria.estg.dei;

import main.pt.ipleiria.estg.dei.db.etl.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.db.etl.DatabaseCreator;
import main.pt.ipleiria.estg.dei.events.IngestModuleProgress;
import main.pt.ipleiria.estg.dei.exceptions.BrowserHistoryIngestModuleExpection;
import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.exceptions.DatabaseInitializationException;
import main.pt.ipleiria.estg.dei.exceptions.MigrationException;
import main.pt.ipleiria.estg.dei.model.browsers.BlockedWebsites;
import main.pt.ipleiria.estg.dei.model.browsers.Chrome;
import main.pt.ipleiria.estg.dei.model.browsers.Firefox;
import main.pt.ipleiria.estg.dei.model.browsers.Module;
import main.pt.ipleiria.estg.dei.utils.Logger;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModule;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModuleProgress;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.autopsy.ingest.IngestModule;
import org.sleuthkit.datamodel.Content;

import java.util.ArrayList;
import java.util.List;


class BrowserHistoryDataSourceIngestModule implements DataSourceIngestModule {
    private Logger logger = new Logger<>(BrowserHistoryDataSourceIngestModule.class);
    private List<Module> modulesToRun;


    BrowserHistoryDataSourceIngestModule(BrowserHistoryModuleIngestJobSettings settings) {
    }

    @Override
    public void startUp(IngestJobContext context) {
        try {
            DatabaseCreator.init();
            modulesToRun = new ArrayList<>();
            modulesToRun.add(new Chrome(context));
            modulesToRun.add(new Firefox(context));
            modulesToRun.add(new BlockedWebsites());
        } catch (MigrationException e) {
            logger.error("Migration couldn't be run. Please look at the logs for more information!");
            throw new BrowserHistoryIngestModuleExpection(e.getMessage());
        } catch (DatabaseInitializationException e) {
            logger.error("Database couldn't be initialized. Please look at the logs for more information!");
            throw new BrowserHistoryIngestModuleExpection(e.getMessage());
        } catch (ConnectionException e) {
            logger.error("Connection couldn't be established. Please look at the logs for more information!");
            throw new BrowserHistoryIngestModuleExpection(e.getMessage());
        }
    }

    @Override
    public ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar) {
        IngestModuleProgress.getInstance().init(dataSource, modulesToRun, progressBar);

        modulesToRun.forEach(browser -> {
            try {
                browser.run(dataSource);
            } catch (ConnectionException e) {
                logger.error("Connection couldn't be established. Please look at the logs for more information!");
                throw new BrowserHistoryIngestModuleExpection(e.getMessage());
            }
        });

        IngestModuleProgress.getInstance().finishProgress();
        DataWarehouseConnection.getInstance().closeConnection();

        return IngestModule.ProcessResult.OK;
    }
}