package main.pt.ipleiria.estg.dei.utils;

import main.pt.ipleiria.estg.dei.BrowserHistoryReportConfigurationPanel;
import main.pt.ipleiria.estg.dei.db.DatasetRepository;
import main.pt.ipleiria.estg.dei.dtos.IndexDto;
import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.exceptions.GenerateReportException;
import main.pt.ipleiria.estg.dei.model.Login;
import main.pt.ipleiria.estg.dei.model.Website;
import main.pt.ipleiria.estg.dei.model.Word;
import main.pt.ipleiria.estg.dei.utils.report.Generator;
import main.pt.ipleiria.estg.dei.utils.report.ReportParameterMap;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileGenerator {
    private Logger<FileGenerator> logger;
    private BrowserHistoryReportConfigurationPanel configPanel;
    private Class from;
    private String reportDir;

    public FileGenerator(BrowserHistoryReportConfigurationPanel configPanel, Class from, String reportDir) {
        this.configPanel = configPanel;
        this.from = from;
        this.reportDir = reportDir;
        logger = new Logger<>(FileGenerator.class);
    }

    public void generatePDF() throws ConnectionException, SQLException, ClassNotFoundException, JRException, GenerateReportException, IOException {
        InputStream templateFile = from.getResourceAsStream("/resources/template/autopsy.jrxml");
        Generator generator = new Generator(templateFile);

        List<String> usernames = configPanel.getUsersSelected();//TODO: Be sure that it is not null...
        String dayAnalised = Utils.parseToDay(configPanel.getDate());

        List<Login> login;
        List<Word> wordsUsed;
        List<Website> activityInWebsite = null;

        if (configPanel.isMultipleUsers()) {
            Map<String, Object> reportData = new HashMap<>();
            login = DatasetRepository.getInstance().getLoginsUsed();
            wordsUsed = DatasetRepository.getInstance().getWordsUsed();

            reportData.put("loginsDataSource", new JRBeanCollectionDataSource(login));
            reportData.put("mostVisitedWebsites", new JRBeanCollectionDataSource(DatasetRepository.getInstance().getMostVisitedWebsite(configPanel.getVisitsAmountOfElementsChart())));
            reportData.put("blockedVisitedWebsites", new JRBeanCollectionDataSource(DatasetRepository.getInstance().getBlockedVisitedWebsite()));
            reportData.put("wordsDataSource", new JRBeanCollectionDataSource(wordsUsed));

            if (!configPanel.getWebsites().isEmpty()) {
                activityInWebsite =  DatasetRepository.getInstance().getActivityInWebsite(configPanel.getWebsites());
                reportData.put("websiteDetailDataSource", new JRBeanCollectionDataSource(activityInWebsite));
            }

            reportData.put("websiteVisitedInPeriodOfTimeDataSource", new JRBeanCollectionDataSource(DatasetRepository.getInstance().getVisitedWebsiteInDay(configPanel.getDate())));
            reportData.put("websiteVisitedInDay", dayAnalised);
            reportData.put("indexDataSource", new JRBeanCollectionDataSource(generateIndex(new Double(login.size()), new Double(wordsUsed.size()), new Double(activityInWebsite != null ? activityInWebsite.size() : 0) )));
            generate(generator, reportData, "GlobalSearch");
            templateFile.reset();
        }
        for (String username: usernames ) {
            Map<String, Object> reportData = new HashMap<>();
            login = DatasetRepository.getInstance().getLoginsUsed(username);
            wordsUsed = DatasetRepository.getInstance().getWordsUsed(username);

            reportData.put("loginsDataSource", new JRBeanCollectionDataSource(login));
            reportData.put("mostVisitedWebsites", new JRBeanCollectionDataSource(DatasetRepository.getInstance().getMostVisitedWebsite(configPanel.getVisitsAmountOfElementsChart(), username)));
            reportData.put("blockedVisitedWebsites", new JRBeanCollectionDataSource(DatasetRepository.getInstance().getBlockedVisitedWebsite(username)));
            reportData.put("wordsDataSource", new JRBeanCollectionDataSource(wordsUsed));
            if (!configPanel.getWebsites().isEmpty()) {
                activityInWebsite = DatasetRepository.getInstance().getActivityInWebsite(configPanel.getWebsites(), username);
                reportData.put("websiteDetailDataSource", new JRBeanCollectionDataSource(activityInWebsite));
            }
            reportData.put("websiteVisitedInPeriodOfTimeDataSource", new JRBeanCollectionDataSource(DatasetRepository.getInstance().getVisitedWebsiteInDay(username, configPanel.getDate())));
            reportData.put("websiteVisitedInDay", dayAnalised);
            reportData.put("indexDataSource", new JRBeanCollectionDataSource(generateIndex(new Double(login.size()), new Double(wordsUsed.size()), new Double(activityInWebsite != null ? activityInWebsite.size() : 0) )));
            generate(generator, reportData, username);
            templateFile.reset();
        }
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
        try(OutputStream outputStream = new FileOutputStream(reportDir + "\\"+ username+".pdf")) {
            byteArrayOutputStream.writeTo(outputStream);
        }
        byteArrayOutputStream.close();

    }


    public  void generateCSV() {
        Map<String, String> queries = configPanel.getQueries();
        if (!queries.isEmpty()) {
            queries.forEach((key, value) -> {
                try {
                    Utils.writeCsv(DatasetRepository.getInstance().execute(value), reportDir + "\\" + key);
                } catch (ConnectionException | ClassNotFoundException | SQLException e) {
                    logger.warn("Couldn't extract query: " + value);
                }
            });
        }
    }
}
