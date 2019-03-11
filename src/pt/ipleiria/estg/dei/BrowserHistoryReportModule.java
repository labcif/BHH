package pt.ipleiria.estg.dei;

import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.autopsy.ingest.IngestServices;
import org.sleuthkit.autopsy.report.GeneralReportModule;
import org.sleuthkit.autopsy.report.ReportProgressPanel;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.TskCoreException;
import pt.ipleiria.estg.dei.exceptions.GenerateReportException;
import pt.ipleiria.estg.dei.model.GoogleChrome;
import pt.ipleiria.estg.dei.utils.Utils;
import pt.ipleiria.estg.dei.utils.report.Generator;
import pt.ipleiria.estg.dei.utils.report.ReportParameterMap;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrowserHistoryReportModule implements GeneralReportModule {

    private static BrowserHistoryReportModule instance;
    public static final String ARTIFACT_TYPE_BROWSER_HISTORY = "type_browser_history";

    @Override
    public void generateReport(String reportDir, ReportProgressPanel reportProgressPanel) {

        reportProgressPanel.setIndeterminate(false);
        reportProgressPanel.start();
        reportProgressPanel.updateStatusLabel("Adding files...");

        // Example
        StringBuilder sb =new StringBuilder();
        sb.append("The most used urls are: \n");
        List<GoogleChrome> visits = new ArrayList<>();
        try {

            ArrayList<BlackboardArtifact> artifacts = Case.getCurrentCase()
                    .getSleuthkitCase()
                    .getBlackboardArtifacts(ARTIFACT_TYPE_BROWSER_HISTORY);
            artifacts
                    .get(artifacts.size()-1)
                    .getAttributes()
                    .forEach(att -> {
                        GoogleChrome google = (GoogleChrome) Utils.fromByte(att.getValueBytes());
                        sb.append(google).append("\n");
                        visits.add(google);
                    });

        } catch (TskCoreException e) {
            e.printStackTrace();
        }

        File templateFile = new File("src/pt/ipleiria/estg/dei/template/autopsy.jrxml");//TODO: this must be more dynamic
        Generator generator = new Generator(templateFile);

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("Title", sb.toString());
        JRBeanCollectionDataSource jrBeanCollectionDataSource = new JRBeanCollectionDataSource(visits);
        reportData.put("Visits", jrBeanCollectionDataSource);

        generator.setReportData(reportData);

        try {
            ReportParameterMap reportParameters = new ReportParameterMap();
                    // Generate the document into a byte array.
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            reportParameters.setOutputStream(byteArrayOutputStream);
            generator.setReportParameters(reportParameters);

            generator.generateReport();

            try(OutputStream outputStream = new FileOutputStream(reportDir + "\\generatedReport.pdf")) {//TODO: this file must have a timestamp as a name to not override the ones created before
                byteArrayOutputStream.writeTo(outputStream);
            }

        } catch(IOException | GenerateReportException e){
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