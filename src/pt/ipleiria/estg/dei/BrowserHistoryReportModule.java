package pt.ipleiria.estg.dei;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.autopsy.ingest.IngestServices;
import org.sleuthkit.autopsy.report.GeneralReportModule;
import org.sleuthkit.autopsy.report.ReportProgressPanel;
import pt.ipleiria.estg.dei.Example.BlakboardExampleIngestModuleFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class BrowserHistoryReportModule implements GeneralReportModule {

    // Static instance of this report
    private static BrowserHistoryReportModule instance;
    private XWPFDocument ForensicReport_doc = null;
    private FileOutputStream fop = null;
    private File file = null;

    @Override
    public void generateReport(String reportDir, ReportProgressPanel reportProgressPanel) {

        reportProgressPanel.setIndeterminate(false);
        reportProgressPanel.start();
        reportProgressPanel.updateStatusLabel("Adding files...");


        // Example
        String content = "Testing 123";
        // get the content in bytes
        byte[] contentInBytes = content.getBytes();


        //Saving into Assigned Dir
        try {
            file= new File(reportDir+ System.getProperty("file.separator") + "generatedReport.txt");
            fop  = new FileOutputStream(file);

            if (!file.exists()) {
                file.createNewFile();
            }

            fop.write(contentInBytes);
            fop.close();
        } catch(IOException e){
            IngestMessage message = IngestMessage.createMessage( IngestMessage.MessageType.INFO, BrowserHistoryReportModule.getDefault().getName(),"Failed to create report");
            IngestServices.getInstance().postMessage(message);
        }

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
        return new BrowserHistoryReportConfigurationPanel(); //To change body of generated methods, choose Tools | Templates.
    }

    // Get the default instance of this report
    public static synchronized BrowserHistoryReportModule getDefault() {
        if (instance == null) {
            instance = new BrowserHistoryReportModule();
        }
        return instance;
    }
}