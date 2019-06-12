package main.pt.ipleiria.estg.dei.labcif.bhh.utils;

import main.pt.ipleiria.estg.dei.labcif.bhh.database.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.labcif.bhh.database.DatasetRepository;
import main.pt.ipleiria.estg.dei.labcif.bhh.dtos.IndexDto;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.BrowserHistoryReportModuleExpection;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.GenerateReportException;
import main.pt.ipleiria.estg.dei.labcif.bhh.models.Login;
import main.pt.ipleiria.estg.dei.labcif.bhh.models.Website;
import main.pt.ipleiria.estg.dei.labcif.bhh.models.Word;
import main.pt.ipleiria.estg.dei.labcif.bhh.panels.reportModulePanel.BrowserHistoryReportConfigurationPanel;
import main.pt.ipleiria.estg.dei.labcif.bhh.utils.report.Generator;
import main.pt.ipleiria.estg.dei.labcif.bhh.utils.report.ReportParameterMap;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

public class FileGenerator {
    private LoggerBHH<FileGenerator> loggerBHH;
    private BrowserHistoryReportConfigurationPanel configPanel;
    private Class from;
    private String reportDir;
    private DatasetRepository datasetRepository;

    public FileGenerator(BrowserHistoryReportConfigurationPanel configPanel, Class from, String reportDir, String databaseDirectory) {
        this.configPanel = configPanel;
        this.from = from;
        this.reportDir = reportDir;
        loggerBHH = new LoggerBHH<>(FileGenerator.class);
        try {
            datasetRepository = new DatasetRepository(databaseDirectory);
        } catch (ConnectionException e) {
            loggerBHH.error(e.getMessage());
        }
    }

    public void generatePDF() throws SQLException, GenerateReportException, IOException {
        InputStream templateFile = from.getResourceAsStream("/resources/template/autopsy.jrxml");
        Generator generator = new Generator(templateFile);

        List<String> usernames = configPanel.getUsersSelected();
        if (usernames == null) {
            throw new BrowserHistoryReportModuleExpection("No users selected");
        }
        String dayAnalised = Utils.parseToDay(configPanel.getDate());

        List<Login> login;
        List<Word> wordsUsed;
        List<Website> activityInWebsite = null;

        if (configPanel.isMultipleUsers()) {
            Map<String, Object> reportData = new HashMap<>();
            login = datasetRepository.getLoginsUsed();
            wordsUsed = datasetRepository.getWordsUsed();

            reportData.put("loginsDataSource", new JRBeanCollectionDataSource(login));
            reportData.put("mostVisitedWebsites", new JRBeanCollectionDataSource(datasetRepository.getMostVisitedWebsite(configPanel.getVisitsAmountOfElementsChart())));
            reportData.put("blockedVisitedWebsites", new JRBeanCollectionDataSource(datasetRepository.getBlockedVisitedWebsite()));
            reportData.put("wordsDataSource", new JRBeanCollectionDataSource(wordsUsed));

            if (!configPanel.getWebsites().isEmpty()) {
                activityInWebsite =  datasetRepository.getActivityInWebsite(configPanel.getWebsites());
                reportData.put("websiteDetailDataSource", new JRBeanCollectionDataSource(activityInWebsite));
            }

            reportData.put("websiteVisitedInPeriodOfTimeDataSource", new JRBeanCollectionDataSource(datasetRepository.getVisitedWebsiteInDay(configPanel.getDate())));
            reportData.put("websiteVisitedInDay", dayAnalised);
            reportData.put("indexDataSource", new JRBeanCollectionDataSource(generateIndex(new Double(login.size()), new Double(wordsUsed.size()), new Double(activityInWebsite != null ? activityInWebsite.size() : 0) )));
            generate(generator, reportData, "GlobalSearch");
            templateFile = resetTemplateStream(templateFile);
        }
        for (String username: usernames ) {
            Map<String, Object> reportData = new HashMap<>();
            login = datasetRepository.getLoginsUsed(username);
            wordsUsed = datasetRepository.getWordsUsed(username);

            reportData.put("loginsDataSource", new JRBeanCollectionDataSource(login));
            reportData.put("mostVisitedWebsites", new JRBeanCollectionDataSource(datasetRepository.getMostVisitedWebsite(configPanel.getVisitsAmountOfElementsChart(), username)));
            reportData.put("blockedVisitedWebsites", new JRBeanCollectionDataSource(datasetRepository.getBlockedVisitedWebsite(username)));
            reportData.put("wordsDataSource", new JRBeanCollectionDataSource(wordsUsed));
            if (!configPanel.getWebsites().isEmpty()) {
                activityInWebsite = datasetRepository.getActivityInWebsite(configPanel.getWebsites(), username);
                reportData.put("websiteDetailDataSource", new JRBeanCollectionDataSource(activityInWebsite));
            }
            reportData.put("websiteVisitedInPeriodOfTimeDataSource", new JRBeanCollectionDataSource(datasetRepository.getVisitedWebsiteInDay(username, configPanel.getDate())));
            reportData.put("websiteVisitedInDay", dayAnalised);
            reportData.put("indexDataSource", new JRBeanCollectionDataSource(generateIndex(new Double(login.size()), new Double(wordsUsed.size()), new Double(activityInWebsite != null ? activityInWebsite.size() : 0) )));
            generate(generator, reportData, username);
            templateFile = resetTemplateStream(templateFile);
        }
    }

    private InputStream resetTemplateStream(InputStream templateStream) {
        try {
            templateStream.close();
        } catch (IOException e) {
            loggerBHH.warn(e.getMessage());
        }
        return from.getResourceAsStream("/resources/template/autopsy.jrxml");
    }

    private List<IndexDto> generateIndex(double loginSize, double wordsSearch, double activityWebsites ){
        List<IndexDto> index = new ArrayList<>();
        int pageIndex = 0;
        int pageNumber = 3;

        index.add(new IndexDto(++pageIndex + " - Login found ................................................ " + pageNumber )); // Page 3 Start

        //Number of Logins (divided by (max) row count)
        pageNumber = getListPageNumber(pageNumber, loginSize);
        index.add(new IndexDto(++pageIndex + " - Most visited websites ................................. " + pageNumber )); // Page After Logins
        index.add(new IndexDto(++pageIndex + " - Blocked websites ....................................... " + pageNumber )); // Page After Logins
        index.add(new IndexDto(++pageIndex + " - Words Search ............................................ " + ++pageNumber )); // Page After Logins + 1

        //Number of Words (divided by (max) row count)
        pageNumber = getListPageNumber(pageNumber, wordsSearch);
        index.add(new IndexDto(++pageIndex + " - Activity in Websites .................................... " + pageNumber )); // Page After Words Search

        //Number of Website Visits (divided by (max) row count)
        pageNumber = getListPageNumber(pageNumber, activityWebsites);
        index.add(new IndexDto(++pageIndex + " - Activity in  period of time ............................ " + pageNumber )); // Page After Activity in Websites
        return index;
    }

    public int getListPageNumber(int pageNumber, double size){
        return (int) Math.ceil(size/29) + pageNumber != pageNumber ? (int) Math.ceil(size/29) + pageNumber : ++pageNumber;
    }

    private void generate(Generator generator, Map<String, Object> reportData, String username) throws GenerateReportException, IOException {
        // Images of the report
        reportData.put("imgAutopsyLogo", from.getResource("/resources/images/img_1_autopsy_logo.png").toString());
        reportData.put("imgArrowUp",from.getResource("/resources/images/img_2_arrow_up_icon.png").toString());
        ReportParameterMap reportParameters = new ReportParameterMap();
        generator.setReportData(reportData);
        // Generate the document into a byte array.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        reportParameters.setOutputStream(byteArrayOutputStream);
        generator.setReportParameters(reportParameters);

        generator.generateReport();
        Path path = Paths.get(reportDir, username + ".pdf");
        try(OutputStream outputStream = new FileOutputStream(path.toString())) {
            byteArrayOutputStream.writeTo(outputStream);
        }
        byteArrayOutputStream.close();

    }


    public  void generateCSV() {
        Map<String, String> queries = configPanel.getQueries();
        if (!queries.isEmpty()) {
            queries.forEach((key, value) -> {
                try {
                    Path path = Paths.get(reportDir, key);
                    Utils.writeCsv(datasetRepository.execute(value), path.toString());
                } catch (SQLException e) {
                    loggerBHH.warn("Couldn't extract query: " + value);
                }
            });
        }
    }

    public void generateServer() throws IOException {
        InputStream from = this.from.getResourceAsStream("/resources/server/browser-history-app-1.0.0.jar");
        Path path = Paths.get(reportDir,  "server.jar");
        File fileDest = new File(path.toString());
        copyFile(from, fileDest);

        List<String> databaseDirectory = Collections.singletonList(DataWarehouseConnection.getFullPathConnection());
        Path file = Paths.get(reportDir , "bd_location.txt");
        Files.write(file, databaseDirectory, Charset.forName("UTF-8"));
    }

    private void copyFile(InputStream source, File dest) throws IOException {
        Files.copy(source, dest.toPath());
    }
}
