package main.pt.ipleiria.estg.dei.utils;

import main.pt.ipleiria.estg.dei.BrowserHistoryReportConfigurationPanel;
import main.pt.ipleiria.estg.dei.db.DatasetRepository;
import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.exceptions.GenerateReportException;
import main.pt.ipleiria.estg.dei.model.adapters.UserInfo;
import main.pt.ipleiria.estg.dei.utils.report.Generator;
import main.pt.ipleiria.estg.dei.utils.report.ReportParameterMap;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.*;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

        Map<String, Object> reportData = new HashMap<>();
        List<UserInfo> userInfoDataSource = new ArrayList<>();

        List<String> userNames = configPanel.getUsersSelected();//TODO: Be sure that it is not null...

        for (String nome: userNames ) {
            userInfoDataSource.add(
                    new UserInfo(nome,
                            DatasetRepository.getInstance().getTopVisitedWebsiteByUser(7, nome),
                            DatasetRepository.getInstance().getBlockedWebsiteVisited(7, nome)));
        }

        //Information by user
        reportData.put("userInfoDataSource", new JRBeanCollectionDataSource(userInfoDataSource));

        // Type of chart
        reportData.put("chartType", getChartType());

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
    }

    private JasperReport getChartType() throws JRException {
        InputStream chartTipe = configPanel.isChartBarTipe()?
                from.getResourceAsStream("/resources/template/user_graf.jrxml"):
                from.getResourceAsStream("/resources/template/user_graf_pie.jrxml");
        return JasperCompileManager.compileReport(chartTipe);
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
