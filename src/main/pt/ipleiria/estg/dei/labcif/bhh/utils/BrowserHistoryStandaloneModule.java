package main.pt.ipleiria.estg.dei.labcif.bhh.utils;

import main.pt.ipleiria.estg.dei.labcif.bhh.database.DatabaseCreator;
import main.pt.ipleiria.estg.dei.labcif.bhh.database.DatasetRepository;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.*;
import main.pt.ipleiria.estg.dei.labcif.bhh.modules.ChromeModule;
import main.pt.ipleiria.estg.dei.labcif.bhh.modules.FirefoxModule;
import main.pt.ipleiria.estg.dei.labcif.bhh.modules.Module;
import main.pt.ipleiria.estg.dei.labcif.bhh.modules.SpecialWebsiteModule;
import main.pt.ipleiria.estg.dei.labcif.bhh.panels.reportModulePanel.BrowserHistoryReportConfigurationPanel;
import net.sf.jasperreports.engine.JRException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BrowserHistoryStandaloneModule {
    private List<Module> modules;
    private LoggerBHH loggerBHH;
    private String caseDirectory;

    public BrowserHistoryStandaloneModule(String caseDirectory) {
        modules = new ArrayList<>();
        loggerBHH = new LoggerBHH<>(BrowserHistoryStandaloneModule.class);
        try {
            this.caseDirectory = caseDirectory;
            DatabaseCreator.init(caseDirectory);
        } catch (MigrationException | DatabaseInitializationException | ConnectionException e) {
            loggerBHH.error(e.getMessage());
        }
    }

    public void executeExtraction(boolean isCustomListSelected, String locationCustomWebsiteList) {
        setupModules(isCustomListSelected, locationCustomWebsiteList);

        loggerBHH.info("[EXTRACTION] - Started");
        modules.forEach(module -> {
            try {
                module.run(caseDirectory);
            } catch (ConnectionException e) {
                loggerBHH.error(e.getMessage());
            }
        });
        try {
            DatasetRepository datasetRepository = new DatasetRepository(caseDirectory);
            datasetRepository.addToInfoExtract(OperatingSystemUtils.getComputerName());
            loggerBHH.info("[EXTRACTION] - Finished");
        } catch (ConnectionException | SQLException | ClassNotFoundException e) {
            loggerBHH.error("Info about the extraction couldn't be saved: " + e.getMessage());
        }


    }

    private void setupModules(boolean isCustomListSelected, String locationCustomWebsiteList) {
        modules.add(new ChromeModule(caseDirectory));
        modules.add(new FirefoxModule(caseDirectory));
        if (isCustomListSelected) {
            modules.add(new SpecialWebsiteModule(locationCustomWebsiteList, caseDirectory));
        } else {
            modules.add(new SpecialWebsiteModule(caseDirectory));
        }
    }

    public void executeTransformation( BrowserHistoryReportConfigurationPanel configPanel) {
        loggerBHH.info("Creating report directory");
        String reportDirector = Utils.createDirectoryIfNotExists(caseDirectory + "/report/");
        String currentReportDirectory = Utils.createDirectoryIfNotExists(reportDirector + Utils.getTimestamp() + "/");


        FileGenerator fileGenerator = new FileGenerator(configPanel, getClass(), currentReportDirectory, caseDirectory);

        try {
            loggerBHH.info("[REPORT MODULE]: generating server...");
            fileGenerator.generateServer();
            loggerBHH.info("[REPORT MODULE]: server generated with success");
            loggerBHH.info("[REPORT MODULE]: generating csv...");
            fileGenerator.generateCSV();
            loggerBHH.info("[REPORT MODULE]: csv generated with success");
            loggerBHH.info("[REPORT MODULE]: generating PDF...");
            fileGenerator.generatePDF();
            loggerBHH.info("[REPORT MODULE]: PDF generated with success");
        } catch (IOException | SQLException | GenerateReportException | BrowserHistoryReportModuleExpection e) {
            loggerBHH.error(e.getMessage());
        }
    }
}
