package main.pt.ipleiria.estg.dei;

import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.exceptions.GenerateReportException;
import main.pt.ipleiria.estg.dei.utils.FileGenerator;
import main.pt.ipleiria.estg.dei.utils.Logger;
import net.sf.jasperreports.engine.JRException;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.report.GeneralReportModule;
import org.sleuthkit.autopsy.report.ReportProgressPanel;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;

public class BrowserHistoryReportModule implements GeneralReportModule {

    private static BrowserHistoryReportModule instance;
    private Logger logger = new Logger<>(BrowserHistoryReportModule.class);
    private BrowserHistoryReportConfigurationPanel configPanel;

    @Override
    public void generateReport(String reportDir, ReportProgressPanel reportProgressPanel) {
        reportProgressPanel.setIndeterminate(false);
        reportProgressPanel.start();
        reportProgressPanel.updateStatusLabel("Adding files...");

        try {
            FileGenerator fileGenerator = new FileGenerator(configPanel, getClass(), reportDir);
            fileGenerator.generateServer();
            fileGenerator.generateCSV();
//            fileGenerator.generatePDF(); TODO: Now we are focusing on server, so we let it commented to speed up the process
//        } catch (SQLException | ConnectionException | ClassNotFoundException e) {
//            logger.error(NbBundle.getMessage(this.getClass(), "BrowserHistory.connectionError"));
//        } catch (JRException | GenerateReportException e) {
//            logger.error("Error generating report. Reason: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Error writing on disk. Reason: " + e.getMessage());
        }
        reportProgressPanel.complete(ReportProgressPanel.ReportStatus.COMPLETE);
    }

    @Override
    public String getName() {
        return  NbBundle.getMessage(BrowserHistoryReportModule.class, "BrowserHistoryReportModule.moduleName");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(BrowserHistoryReportModule.class, "BrowserHistoryReportModule.moduleDescription");
    }

    @Override
    public String getRelativeFilePath() {
        return "1.0.0";
    }

    @Override
    public JPanel getConfigurationPanel() {
        if (configPanel == null) {
            configPanel = new BrowserHistoryReportConfigurationPanel();
        }
        return configPanel;
    }

    public static synchronized BrowserHistoryReportModule getDefault() {
        if (instance == null) {
            instance = new BrowserHistoryReportModule();
        }
        return instance;
    }
}