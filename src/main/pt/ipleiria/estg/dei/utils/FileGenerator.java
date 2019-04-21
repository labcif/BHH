package main.pt.ipleiria.estg.dei.utils;

import main.pt.ipleiria.estg.dei.BrowserHistoryReportConfigurationPanel;
import main.pt.ipleiria.estg.dei.db.DatasetRepository;
import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.exceptions.GenerateReportException;
import main.pt.ipleiria.estg.dei.utils.report.Generator;
import main.pt.ipleiria.estg.dei.utils.report.ReportParameterMap;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.*;
import java.sql.SQLException;
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


        if (usernames.size() > 1) {
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("loginsDataSource", new JRBeanCollectionDataSource(DatasetRepository.getInstance().getLoginsUsed()));
            reportData.put("mostVisitedWebsites", new JRBeanCollectionDataSource(DatasetRepository.getInstance().getMostVisitedWebsite(10)));
            reportData.put("blockedVisitedWebsites", new JRBeanCollectionDataSource(DatasetRepository.getInstance().getBlockedVisitedWebsite()));
            reportData.put("wordsDataSource", new JRBeanCollectionDataSource(DatasetRepository.getInstance().getWordsUsed()));
            if (!configPanel.getWebsites().isEmpty()) {
                reportData.put("websiteDetailDataSource", new JRBeanCollectionDataSource(DatasetRepository.getInstance().getActivityInWebsite(configPanel.getWebsites())));
            }
            generate(generator, reportData, "GlobalSearch");
            templateFile.reset();
        }
        for (String username: usernames ) {
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("loginsDataSource", new JRBeanCollectionDataSource(DatasetRepository.getInstance().getLoginsUsed(username)));
            reportData.put("mostVisitedWebsites", new JRBeanCollectionDataSource(DatasetRepository.getInstance().getMostVisitedWebsite(10, username)));
            reportData.put("blockedVisitedWebsites", new JRBeanCollectionDataSource(DatasetRepository.getInstance().getBlockedVisitedWebsite(username)));
            reportData.put("wordsDataSource", new JRBeanCollectionDataSource(DatasetRepository.getInstance().getWordsUsed(username)));
            if (!configPanel.getWebsites().isEmpty()) {
                reportData.put("websiteDetailDataSource", new JRBeanCollectionDataSource(DatasetRepository.getInstance().getActivityInWebsite(configPanel.getWebsites(), username)));
            }

            generate(generator, reportData, username);
            templateFile.reset();
        }
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
