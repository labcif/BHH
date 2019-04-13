package main.pt.ipleiria.estg.dei;

import main.pt.ipleiria.estg.dei.db.DatasetRepository;
import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.exceptions.GenerateReportException;
import main.pt.ipleiria.estg.dei.model.*;
import main.pt.ipleiria.estg.dei.utils.report.Generator;
import main.pt.ipleiria.estg.dei.utils.report.ReportParameterMap;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.autopsy.ingest.IngestServices;
import org.sleuthkit.autopsy.report.GeneralReportModule;
import org.sleuthkit.autopsy.report.ReportProgressPanel;

import javax.swing.*;
import java.io.*;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class BrowserHistoryReportModule implements GeneralReportModule {

    private static BrowserHistoryReportModule instance;

    private BrowserHistoryReportConfigurationPanel configPanel;

    @Override
    public void generateReport(String reportDir, ReportProgressPanel reportProgressPanel) {
        reportProgressPanel.setIndeterminate(false);
        reportProgressPanel.start();
        reportProgressPanel.updateStatusLabel("Adding files...");

       try {
            InputStream templateFile = getClass().getResourceAsStream("/resources/template/autopsy.jrxml");

            Generator generator = new Generator(templateFile);

            Map<String, Object> reportData = new HashMap<>();

            reportData.put("isMostVisitedSitesEnabled", configPanel.isMostVisitedSitesEnabled());
            if(configPanel.isMostVisitedSitesEnabled()) {
                List<Website> topMostVisited = DatasetRepository.getInstance().getTopVisitedWebsite(10);
                reportData.put("Title", "Most visited websites");
                JRBeanCollectionDataSource jrBeanCollectionDataSource = new JRBeanCollectionDataSource(topMostVisited);
                reportData.put("Visits", jrBeanCollectionDataSource);
            }

            reportData.put("isBlokedSitesEnabled", configPanel.isBlokedSitesEnabled());
            if(configPanel.isBlokedSitesEnabled()) {
                List<Website> blockedWebsitesVisited = DatasetRepository.getInstance().getBlockedWebsiteVisited();
                JRBeanCollectionDataSource jrBeanCollectionDataSource = new JRBeanCollectionDataSource(blockedWebsitesVisited);
                reportData.put("Blocked", jrBeanCollectionDataSource);
            }

            reportData.put("isWordsSearchEnabled", configPanel.isWordsSearchEnabled());
            if(configPanel.isWordsSearchEnabled()) {
                List<Word> wordUsed = DatasetRepository.getInstance().getWordsUsed();
                JRBeanCollectionDataSource jrBeanCollectionDataSource = new JRBeanCollectionDataSource(wordUsed);
                reportData.put("Words", jrBeanCollectionDataSource);
            }

            if(configPanel.isWordsSearchEnabled()) {
                List<Email> emailsUsed = DatasetRepository.getInstance().getEmailsUsed();
                JRBeanCollectionDataSource jrBeanCollectionDataSource = new JRBeanCollectionDataSource(emailsUsed);
                reportData.put("Emails", jrBeanCollectionDataSource);
            }


           //Adding a new var for testing, since when printing if a graph uses the same var only the first one will be used
           List<User> usersVisitedSites = new ArrayList<>();
           List<User> usersBlockedSites = new ArrayList<>();
           List<Website> topVisitedWebsiteByUser;
           List<Website> blockedWebsiteVisited;

           List<String> userNames = configPanel.getUsersSelected();//TODO: Be sure that it is not null...

           for (String nome: userNames ) {
              topVisitedWebsiteByUser = DatasetRepository.getInstance().getTopVisitedWebsiteByUser(7, nome);
               blockedWebsiteVisited = DatasetRepository.getInstance().getBlockedWebsiteVisited(7, nome);
               if(!topVisitedWebsiteByUser.isEmpty()){
                   usersVisitedSites.add(new User(nome, new JRBeanCollectionDataSource(topVisitedWebsiteByUser)));
               }
               if(!blockedWebsiteVisited.isEmpty()) {
                   usersBlockedSites.add(new User(nome, new JRBeanCollectionDataSource(blockedWebsiteVisited)));
               }
           }
           
           //Temporary prob there is a way to show a list of string with no class associated
           JRDataSource userList = new JRBeanCollectionDataSource(usersVisitedSites);
           reportData.put("UsersList", userList);
           
           
           //Visited Subreport List (by user) 
           JRDataSource jrBeanCollectionDataSource3 = new JRBeanCollectionDataSource(usersVisitedSites);
           reportData.put("UserVisitsSubreport", jrBeanCollectionDataSource3);
           
           //Blocked Subreport List (by user)       
           JRDataSource userBlockedList = new JRBeanCollectionDataSource(usersBlockedSites);
           reportData.put("UserBlockedSubreport", userBlockedList);




            //Adding SubReport (most people online said that if you want to repeat an element / group of elements the best way is to place them in a subreport and pass the variables)
           InputStream templateFile2 = getClass().getResourceAsStream("/resources/template/user_graf.jrxml");
           JasperReport subReport = JasperCompileManager.compileReport(templateFile2);
           reportData.put("subReport", subReport);


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

        } catch(IOException | GenerateReportException | SQLException | JRException | ConnectionException | ClassNotFoundException e){
            IngestMessage message = IngestMessage.createMessage( IngestMessage.MessageType.INFO, BrowserHistoryReportModule.getDefault().getName(),"Failed to create report");
            IngestServices.getInstance().postMessage(message);
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