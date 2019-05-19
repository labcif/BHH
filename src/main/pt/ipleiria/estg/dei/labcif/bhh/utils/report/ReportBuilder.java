package main.pt.ipleiria.estg.dei.labcif.bhh.utils.report;

import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.GenerateReportException;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.LocalJasperReportsContext;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import org.sleuthkit.autopsy.coreutils.Logger;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Level;

public class ReportBuilder {
    private static final Logger logger = Logger.getLogger(ReportBuilder.class.getName());


    public OutputStream createReport(InputStream templateFile, Map<String, Object> reportDataMap,
                                     ReportParameterMap reportParameters) throws GenerateReportException {
        JasperReport compiledReport;

        // Template compilation
        try {
            logger.log(Level.FINE, "Compiling the report templates.");
            compiledReport = compileReport(templateFile);
        } catch (JRException e) {
            throw new GenerateReportException("500102", e.getMessage());
        }

        try {

            logger.log(Level.FINE, "Loading report's extra data.");
            // Load extra data into the report
            Map<String, Object> reportData = loadReportExtraData(reportDataMap, reportParameters);

            logger.log(Level.FINE, "Writing to the report output directory.");
            // Prepare output paths

            return writeOutputReport(reportParameters, compiledReport, reportData);
        } catch( Exception e ) {
            logger.severe("Execution failed: " + e.getMessage());
            return null;
        }

    }

    //<editor-fold desc="Private Methods">
    private OutputStream writeOutputReport(ReportParameterMap parameters,
                                           JasperReport compiledReport,
                                           Map<String, Object> reportData) throws GenerateReportException {
        SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
        try {

            LocalJasperReportsContext ctx = new LocalJasperReportsContext(DefaultJasperReportsContext.getInstance());
            ctx.setClassLoader(getClass().getClassLoader());
            ctx.setFileResolver(s -> {
                File filePath = new File(Objects.requireNonNull(ReportBuilder.class.getClassLoader().getResource("templates/pdf/" + s)).getFile());
                return filePath;
            });

            JasperFillManager jasperFillManager = JasperFillManager.getInstance(ctx);


            logger.log(Level.FINE, "Generating the report.");
            // Fill pdf metadatas
            JasperPrint print = jasperFillManager.fill(compiledReport, reportData, new JREmptyDataSource());
            logger.log(Level.FINE, "Report Generated.");

            // Generate pdf.
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));

            OutputStream outputStream = parameters.getOutputStream();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.setConfiguration(configuration);
            exporter.exportReport();

            return outputStream;
        } catch (JRException e) {
            throw new GenerateReportException("500107", e.getMessage());
        }
    }

    /**
     * Load extra data to later fill the report variables' with into a hashmap.
     *
     * @return A key-value collection of all the data to fill the report with.
     */
    private Map<String, Object> loadReportExtraData(Map<String, Object> reportDataMap, ReportParameterMap parameters) {
        // Data loading

        //Load resource bundle
        ResourceBundle translations = parameters.getTranslationBundle();
        Locale locale = parameters.getLocale();

        // Map data to report
        Map<String, Object> reportData = new HashMap<>();
        reportData.put(JRParameter.REPORT_LOCALE, locale);
        reportData.put(JRParameter.REPORT_RESOURCE_BUNDLE, translations);

        reportDataMap.forEach(reportData::put);

        return reportData;
    }

    private JasperReport compileReport(InputStream reportFile) throws JRException {
        return JasperCompileManager.compileReport(reportFile);
    }
}
