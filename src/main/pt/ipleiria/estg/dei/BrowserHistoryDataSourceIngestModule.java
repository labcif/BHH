
package main.pt.ipleiria.estg.dei;

import main.pt.ipleiria.estg.dei.db.DatasetRepository;
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
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModule;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModuleProgress;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.datamodel.Content;

import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


class BrowserHistoryDataSourceIngestModule implements DataSourceIngestModule {
    private Logger logger = new Logger<>(BrowserHistoryDataSourceIngestModule.class);
    private List<Module> modulesToRun;
    private BrowserHistoryModuleIngestJobSettings settings;

    BrowserHistoryDataSourceIngestModule(BrowserHistoryModuleIngestJobSettings settings) {
        this.settings = settings;
    }

    @Override
    public void startUp(IngestJobContext context) {
        try {
            DatabaseCreator.init();
            modulesToRun = new ArrayList<>();
            modulesToRun.add(new Chrome(context));
            modulesToRun.add(new Firefox(context));
            if (!settings.isNoneChoosed()) {
                modulesToRun.add(new BlockedWebsites(settings.getFileChoosed()));
            }

        } catch (MigrationException e) {
            logger.error("Migration couldn't be run. Please look at the logs for more information!");
            throw new BrowserHistoryIngestModuleExpection(e.getMessage());
        } catch (DatabaseInitializationException e) {
            logger.error("Database couldn't be initialized. Please look at the logs for more information!");
            throw new BrowserHistoryIngestModuleExpection(e.getMessage());
        } catch (ConnectionException e) {
            logger.error(NbBundle.getMessage(this.getClass(), "BrowserHistory.connectionError"));
            throw new BrowserHistoryIngestModuleExpection(e.getMessage());
        }
    }

    @Override
    public ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar) {
        IngestModuleProgress.getInstance().init(dataSource, modulesToRun, progressBar);
        try {
            if (!isFirstTimeRunning(dataSource.getName())) {
                int result = showConfirmationPanel();
                if (result == JOptionPane.YES_OPTION) {
                    DatasetRepository.getInstance().cleanAllData();
                } else {
                    logger.warn("Ingest Module has been cancelled.");
                    return ProcessResult.OK;
                }
            }
            runModules(dataSource);
            return ProcessResult.OK;
        } catch (SQLException | ConnectionException | ClassNotFoundException e) {
            logger.error(NbBundle.getMessage(this.getClass(), "BrowserHistory.connectionError"));
            throw new BrowserHistoryIngestModuleExpection(e.getMessage());
        }finally {
            IngestModuleProgress.getInstance().finishProgress();
        }
    }

    private int showConfirmationPanel() {
        return JOptionPane.showConfirmDialog(null, "You have already run this image. Proceeding will clean all the database. Do you want to proceed?", "WARNING", JOptionPane.YES_NO_OPTION);
    }
    private void runModules(Content dataSource) throws ConnectionException, SQLException, ClassNotFoundException {
        modulesToRun.forEach(browser -> {
            try {
                browser.run(dataSource);
            } catch (ConnectionException e) {
                logger.error(NbBundle.getMessage(this.getClass(), "BrowserHistory.connectionError"));
                throw new BrowserHistoryIngestModuleExpection(e.getMessage());
            }
        });
        DatasetRepository.getInstance().addToInfoExtract(dataSource.getName());
    }
    private boolean isFirstTimeRunning(String name) throws ConnectionException, SQLException, ClassNotFoundException {
        return DatasetRepository.getInstance().isFirstRunningImage(name);
    }
}