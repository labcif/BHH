package main.pt.ipleiria.estg.dei.utils.report;

import main.pt.ipleiria.estg.dei.exceptions.GenerateReportException;

import java.io.File;
import java.util.Map;

public class Generator {
    private File templateFile;
    private Map<String, Object> reportData;
    private ReportParameterMap reportParameters;

    public Generator(File templateFile) {
        this.templateFile = templateFile;
        reportData = null;
        reportParameters = new ReportParameterMap();
    }

    public void generateReport() throws GenerateReportException {
        ReportBuilder reportBuilder = new ReportBuilder();
        reportBuilder.createReport(this.templateFile, reportData, reportParameters);
    }

    public File getTemplateFile() {
        return templateFile;
    }

    public void setTemplateFile(File templateFile) {
        this.templateFile = templateFile;
    }

    public Map<String, Object> getReportData() {
        return reportData;
    }

    public void setReportData(Map<String, Object> reportData) {
        this.reportData = reportData;
    }

    public ReportParameterMap getReportParameters() {
        return reportParameters;
    }

    public void setReportParameters(ReportParameterMap reportParameters) {
        this.reportParameters = reportParameters;
    }
}
