
package main.pt.ipleiria.estg.dei.labcif.bhh;


import main.pt.ipleiria.estg.dei.labcif.bhh.database.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.labcif.bhh.database.DatabaseCreator;
import main.pt.ipleiria.estg.dei.labcif.bhh.database.DatasetRepository;
import main.pt.ipleiria.estg.dei.labcif.bhh.events.IngestModuleProgress;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.BrowserHistoryIngestModuleExpection;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.DatabaseInitializationException;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.MigrationException;
import main.pt.ipleiria.estg.dei.labcif.bhh.modules.*;
import main.pt.ipleiria.estg.dei.labcif.bhh.panels.ingestModulePanel.BrowserHistoryModuleIngestJobSettings;
import main.pt.ipleiria.estg.dei.labcif.bhh.utils.LoggerBHH;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModule;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModuleProgress;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.datamodel.Content;

import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class BrowserHistoryDataSourceIngestModule implements DataSourceIngestModule {
    private LoggerBHH loggerBHH = new LoggerBHH<>(BrowserHistoryDataSourceIngestModule.class);
    private List<Module> modulesToRun;
    private BrowserHistoryModuleIngestJobSettings settings;
    private DatasetRepository datasetRepository;

    public BrowserHistoryDataSourceIngestModule(BrowserHistoryModuleIngestJobSettings settings) {
        this.settings = settings;
    }

    @Override
    public void startUp(IngestJobContext context) {
        try {
            String databaseDirectory = Case.getCurrentCase().getCaseDirectory();
            DatabaseCreator.init(databaseDirectory);
            modulesToRun = new ArrayList<>();
            modulesToRun.add(new ChromeModule(context, databaseDirectory));
            modulesToRun.add(new FirefoxModule(context, databaseDirectory));
            modulesToRun.add(new BraveModule(context, databaseDirectory));
            modulesToRun.add(new VivaldiModule(context, databaseDirectory));
            if (!settings.isNoneChoosed()) {
                modulesToRun.add(new SpecialWebsiteModule(settings.getFileChoosed(), databaseDirectory));
            }
        } catch (MigrationException e) {
            loggerBHH.error("Migration couldn't be run. Please look at the logs for more information!");
            throw new BrowserHistoryIngestModuleExpection(e.getMessage());
        } catch (DatabaseInitializationException e) {
            loggerBHH.error("Database couldn't be initialized. Please look at the logs for more information!");
            throw new BrowserHistoryIngestModuleExpection(e.getMessage());
        } catch (ConnectionException e) {
            loggerBHH.error(NbBundle.getMessage(this.getClass(), "BrowserHistory.connectionError"));
            throw new BrowserHistoryIngestModuleExpection(e.getMessage());
        } finally {
            DataWarehouseConnection.closeConnection();
        }
    }

    @Override
    public ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar) {
        IngestModuleProgress.getInstance().init(dataSource, modulesToRun, progressBar);
        try {
            datasetRepository = new DatasetRepository(Case.getCurrentCase().getCaseDirectory());
            if (!isFirstTimeRunning(dataSource.getName())) {
                int result = showConfirmationPanel();
                if (result == JOptionPane.YES_OPTION) {
                   datasetRepository.dropAllTables();
                   DatabaseCreator.init(Case.getCurrentCase().getCaseDirectory());
                } else {
                    loggerBHH.warn("Ingest Module has been cancelled.");
                    return ProcessResult.OK;
                }
            }
            runModules(dataSource);
            return ProcessResult.OK;
        } catch (SQLException | ConnectionException | ClassNotFoundException | MigrationException | DatabaseInitializationException e) {
            loggerBHH.error(NbBundle.getMessage(this.getClass(), "BrowserHistory.connectionError") + e.getMessage());
            return ProcessResult.ERROR;
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
                loggerBHH.error(NbBundle.getMessage(this.getClass(), "BrowserHistory.connectionError"));
                throw new BrowserHistoryIngestModuleExpection(e.getMessage());
            }
        });
        datasetRepository.addToInfoExtract(dataSource.getName());
    }
    private boolean isFirstTimeRunning(String name) throws SQLException {
        return datasetRepository.isFirstRunningImage(name);
    }
}