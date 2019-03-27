package main.pt.ipleiria.estg.dei;

import main.pt.ipleiria.estg.dei.dtos.RelativeFrequencyBrowser;
import main.pt.ipleiria.estg.dei.exceptions.BrowserHistoryIngestModuleExpection;
import main.pt.ipleiria.estg.dei.exceptions.GenerateReportException;
import main.pt.ipleiria.estg.dei.model.GoogleChrome;
import main.pt.ipleiria.estg.dei.utils.Utils;
import main.pt.ipleiria.estg.dei.utils.report.Generator;
import main.pt.ipleiria.estg.dei.utils.report.ReportParameterMap;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.autopsy.ingest.IngestServices;
import org.sleuthkit.autopsy.report.GeneralReportModule;
import org.sleuthkit.autopsy.report.ReportProgressPanel;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.TskCoreException;

import javax.swing.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class BrowserHistoryReportModule implements GeneralReportModule {

    private static BrowserHistoryReportModule instance;
    static final String ARTIFACT_TYPE_BROWSER_HISTORY = "type_browser_history";
    static final String ARTIFACT_TYPE_BLOCKED_HISTORY = "type_blocked_history";
    static final String ARTIFACT_TYPE_EMAIL_HISTORY= "type_blocked_history";
    static final String ARTIFACT_TYPE_WORDS_GOOGLE_ENGINE = "Type_words_in_google";
    static final String ARTIFACT_TYPE_FREQUENCY_HISTORY = "Relative_frequency";

    private BrowserHistoryReportConfigurationPanel configPanel;

    @Override
    public void generateReport(String reportDir, ReportProgressPanel reportProgressPanel) {
        reportProgressPanel.setIndeterminate(false);
        reportProgressPanel.start();
        reportProgressPanel.updateStatusLabel("Adding files...");

        StringBuilder sb =new StringBuilder();
        sb.append("The most used urls are: \n");
        List<GoogleChrome> visits = new ArrayList<>();
        List<RelativeFrequencyBrowser> frequencyBrowsers = new ArrayList<>();

        StringBuilder sbBlocked =new StringBuilder();
        sbBlocked.append("The user has visited this blocked Websites: \n");

        StringBuilder sbWordSearchInEngine =new StringBuilder();

        try {
            ArrayList<BlackboardArtifact> artifacts = Case.getCurrentCase()
                    .getSleuthkitCase()
                    .getBlackboardArtifacts(ARTIFACT_TYPE_BROWSER_HISTORY);
            if (artifacts.isEmpty()) {
                //TODO: Will have to decide if this is want we want. Probably in the future we will allow options on ingest module
                //TODO: This will likely imply that if I didnt run that option, probably here I only want to ignore and not throw an error
                throw new BrowserHistoryIngestModuleExpection("Please run Browser History ingest Module before running this report file");
            }
            artifacts
                    .get(artifacts.size()-1)
                    .getAttributes()
                    .forEach(att -> {
                        GoogleChrome google = (GoogleChrome) Utils.fromByte(att.getValueBytes());
                        sb.append(google).append("\n");
                        visits.add(google);
                    });

            //Example
            artifacts = Case.getCurrentCase()
                    .getSleuthkitCase()
                    .getBlackboardArtifacts(ARTIFACT_TYPE_BLOCKED_HISTORY);
            if (!artifacts.isEmpty()) {
                artifacts
                        .get(artifacts.size()-1)
                        .getAttributes()
                        .forEach(att -> {
                            GoogleChrome google = (GoogleChrome) Utils.fromByte(att.getValueBytes());
                            sbBlocked.append(google).append("\n");
                        });
            }

            artifacts = Case.getCurrentCase()
                    .getSleuthkitCase()
                    .getBlackboardArtifacts(ARTIFACT_TYPE_WORDS_GOOGLE_ENGINE);
            if (!artifacts.isEmpty()) {
              artifacts.get(artifacts.size() - 1)
                        .getAttributes().forEach(word-> sbWordSearchInEngine.append(word.getValueString()).append(", "));
            }

            artifacts = Case.getCurrentCase()
                    .getSleuthkitCase()
                    .getBlackboardArtifacts(ARTIFACT_TYPE_FREQUENCY_HISTORY);
            if (!artifacts.isEmpty()) {
                frequencyBrowsers = artifacts.get(artifacts.size()-1)
                        .getAttributes()
                        .stream()
                        .map(browser-> (RelativeFrequencyBrowser)Utils.fromByte(browser.getValueBytes()))
                        .collect(Collectors.toList());
            }


            File templateFile = new File("src/pt/ipleiria/estg/dei/template/autopsy.jrxml");//TODO: this must be more dynamic
            Generator generator = new Generator(templateFile);

            Map<String, Object> reportData = new HashMap<>();

            reportData.put("isMostVisitedSitesEnabled", configPanel.isMostVisitedSitesEnabled());
            if(configPanel.isMostVisitedSitesEnabled()) {
                reportData.put("Title", sb.toString());
                JRBeanCollectionDataSource jrBeanCollectionDataSource = new JRBeanCollectionDataSource(visits);
                reportData.put("Visits", jrBeanCollectionDataSource);
            }

            reportData.put("isDomainDailyVisitsEnabled", configPanel.isDomainDailyVisitsEnabled());
            if(configPanel.isDomainDailyVisitsEnabled()) {
                JRBeanCollectionDataSource jrBeanCollectionDataSource1 = new JRBeanCollectionDataSource(frequencyBrowsers);
                reportData.put("Frequency", jrBeanCollectionDataSource1);
            }

            reportData.put("isBlokedSitesEnabled", configPanel.isBlokedSitesEnabled());
            if(configPanel.isBlokedSitesEnabled()) {
                reportData.put("Blocked", sbBlocked.toString());
            }


            reportData.put("isWordsSearchEnabled", configPanel.isWordsSearchEnabled());
            if(configPanel.isWordsSearchEnabled()) {
                reportData.put("wordsFromGoogleEngine", sbWordSearchInEngine.toString());
            }
            generator.setReportData(reportData);

            ReportParameterMap reportParameters = new ReportParameterMap();
                    // Generate the document into a byte array.
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            reportParameters.setOutputStream(byteArrayOutputStream);
            generator.setReportParameters(reportParameters);

            generator.generateReport();

            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss-SS");
            Date date = new Date();
            String dateNoTime = dateFormat.format(date);

            try(OutputStream outputStream = new FileOutputStream(reportDir + "\\generatedReport"+ dateNoTime +".pdf")) {
                byteArrayOutputStream.writeTo(outputStream);
            }

        } catch(IOException | GenerateReportException | TskCoreException e){
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
        if (configPanel == null) {
            configPanel = new BrowserHistoryReportConfigurationPanel();
        }
        return configPanel;
    }

    // Get the default instance of this report
    public static synchronized BrowserHistoryReportModule getDefault() {
        if (instance == null) {
            instance = new BrowserHistoryReportModule();
        }
        return instance;
    }
}