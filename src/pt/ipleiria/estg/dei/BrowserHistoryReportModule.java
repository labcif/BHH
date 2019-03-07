package pt.ipleiria.estg.dei;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.report.GeneralReportModule;
import org.sleuthkit.autopsy.report.ReportProgressPanel;

import javax.swing.*;

public class BrowserHistoryReportModule implements GeneralReportModule {
    
    // Static instance of this report
    private static BrowserHistoryReportModule instance;
    
    @Override
    public void generateReport(String s, ReportProgressPanel reportProgressPanel) {

        reportProgressPanel.setIndeterminate(false);
        reportProgressPanel.start();
        reportProgressPanel.updateStatusLabel("Adding files...");




        // Set progress panel status to complete
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
        return GeneralReportModule.super.getConfigurationPanel(); //To change body of generated methods, choose Tools | Templates.
    }
    
    // Get the default instance of this report
    public static synchronized BrowserHistoryReportModule getDefault() {
        if (instance == null) {
            instance = new BrowserHistoryReportModule();
        }
        return instance;
    }   
}
