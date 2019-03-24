package main.pt.ipleiria.estg.dei.utils.report;

import net.sf.jasperreports.engine.JasperReport;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class ReportParameterMap extends HashMap<String,Object> {
    public static final String PARAMETER_LOCALE = "locale";
    public static final String PARAMETER_USE_COMPILED_DISABLED= "useCompiledDisabled";
    public static final String TRANSLATION_BUNDLE= "translationBundle";
    public static final String OUTPUT_STREAM= "outputStream";
    public static final String COMPILED_REPORT= "compiledReport";
    public static final String COMPILED_REPORT_PATH= "compiledReportPath";


    public Locale getLocale(){
        return (Locale)this.get(PARAMETER_LOCALE);
    }
    public void setLocale( Locale locale ){
        this.put(PARAMETER_LOCALE, locale);
    }

    public Boolean isCompiledDisabled(){
        return (Boolean)this.get(PARAMETER_USE_COMPILED_DISABLED);
    }
    public void setCompiledDisabled( Boolean isIt){
        this.put(PARAMETER_USE_COMPILED_DISABLED, isIt);
    }

    public ResourceBundle getTranslationBundle(){
        return (ResourceBundle)this.get(TRANSLATION_BUNDLE);
    }
    public void setTranslationBundle( ResourceBundle bundle){
        this.put(TRANSLATION_BUNDLE, bundle);
    }

    public OutputStream getOutputStream(){
        return (OutputStream)this.get(OUTPUT_STREAM);
    }

    public void setOutputStream( OutputStream outputPath){
        this.put(OUTPUT_STREAM, outputPath);
    }

    public JasperReport getCompiledReport(){
        return (JasperReport)this.get(COMPILED_REPORT);
    }
    public void setCompiledReport( JasperReport compiledReport){
        this.put(COMPILED_REPORT, compiledReport);
    }

    public String getCompiledReportPath(){
        return (String)this.get(COMPILED_REPORT_PATH);
    }
    public void setCompiledReportPath( String compiledReportPath){
        this.put(COMPILED_REPORT_PATH, compiledReportPath);
    }
}
