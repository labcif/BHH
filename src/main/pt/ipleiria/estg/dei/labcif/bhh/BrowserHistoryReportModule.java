package main.pt.ipleiria.estg.dei.labcif.bhh;


import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.GenerateReportException;
import main.pt.ipleiria.estg.dei.labcif.bhh.panels.reportModulePanel.BrowserHistoryReportConfigurationPanel;
import main.pt.ipleiria.estg.dei.labcif.bhh.utils.FileGenerator;
import main.pt.ipleiria.estg.dei.labcif.bhh.utils.LoggerBHH;
import net.sf.jasperreports.engine.JRException;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.report.GeneralReportModule;
import org.sleuthkit.autopsy.report.ReportProgressPanel;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;

public class BrowserHistoryReportModule implements GeneralReportModule {

    private static BrowserHistoryReportModule instance;
    private LoggerBHH loggerBHH = new LoggerBHH<>(BrowserHistoryReportModule.class);
    private BrowserHistoryReportConfigurationPanel configPanel;

    @Override
    public void generateReport(String reportDir, ReportProgressPanel reportProgressPanel) {
        reportProgressPanel.setIndeterminate(false);
        reportProgressPanel.start();
        reportProgressPanel.updateStatusLabel("Adding files...");

        try {
            FileGenerator fileGenerator = new FileGenerator(configPanel, getClass(), reportDir, Case.getCurrentCase().getCaseDirectory());
            fileGenerator.generateServer();
            fileGenerator.generateCSV();
            fileGenerator.generatePDF();
        } catch (SQLException | ConnectionException | ClassNotFoundException e) {
            loggerBHH.error(NbBundle.getMessage(this.getClass(), "BrowserHistory.connectionError"));
        } catch (JRException | GenerateReportException e) {
            loggerBHH.error("Error generating report. Reason: " + e.getMessage());
        } catch (IOException e) {
            loggerBHH.error("Error writing on disk. Reason: " + e.getMessage());
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