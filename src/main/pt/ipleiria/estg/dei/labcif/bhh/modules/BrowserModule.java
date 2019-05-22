package main.pt.ipleiria.estg.dei.labcif.bhh.modules;

import main.pt.ipleiria.estg.dei.labcif.bhh.database.DataWarehouseConnection;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.BrowserHistoryIngestModuleExpection;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.NoCriticalException;
import main.pt.ipleiria.estg.dei.labcif.bhh.utils.LoggerBHH;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.casemodule.services.FileManager;
import org.sleuthkit.autopsy.datamodel.ContentUtils;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.TskCoreException;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


public abstract class BrowserModule extends Module {
    private Case currentCase;
    private LoggerBHH<BrowserModule> loggerBHH = new LoggerBHH<>(BrowserModule.class);
    private IngestJobContext context;
    private Content dataSource;

    protected BrowserModule(IngestJobContext context, String databaseDirectory) {
        super(databaseDirectory);
        try {
            currentCase = Case.getCurrentCaseThrows();
            this.context = context;
        } catch (NoCurrentCaseException e) {
            loggerBHH.error("Case couldn't be find. So state couldn't be initialized");
            throw new BrowserHistoryIngestModuleExpection("Case couldn't be find. So state couldn't be initialized");
        }
    }

    @Override
    public void run(Content dataSource) throws ConnectionException {
        loggerBHH.info("[" + getModuleName() +"] - Started");
        this.dataSource = dataSource;
        runHistory();
        loggerBHH.info("[" + getModuleName() +"] - Finished");
    }

    private void runHistory() throws ConnectionException {
        try {
            FileManager fileManager = currentCase.getServices().getFileManager();
            List<AbstractFile> history = fileManager.findFiles(dataSource,  getHistoryFilename(), getPathToBrowserHistory());
            execute(history, "history-");

            List<AbstractFile> loginData = fileManager.findFiles(dataSource, getLoginDataFilename(), getPathToBrowserHistory());
            execute(loginData, "login-");
        } catch (TskCoreException e) {
            loggerBHH.warn("[" + getModuleName() +"] Issue when running: " + e.getMessage());
        }
    }
    private void execute(List<AbstractFile> files, String prefixName) throws ConnectionException {
        for (AbstractFile file: files) {
            String userName = file.getParentPath().split("/")[2];

            //We have to copy this file to the temp directory
            String tempPath = getTempPath(currentCase, getModuleName()) + File.separator + prefixName + userName + ".db";
            try {
                ContentUtils.writeToFile(file, new File(tempPath), context::dataSourceIngestIsCancelled);
                runETLProcess(tempPath, userName);
            } catch (IOException | NoCriticalException e) {
                loggerBHH.warn(e.getMessage());//We don't want to stop the process when it is a non critical exception
            }
        }
    }

    protected void insertWordInTable(ResultSet rs, String user) throws ConnectionException, SQLException, ClassNotFoundException {
        PreparedStatement preparedStatement =  DataWarehouseConnection.getConnection(databaseDirectory).prepareStatement(
                " INSERT INTO t_clean_search_in_engines (search_in_engines_words, search_in_engines_source_full, search_in_engines_user_origin, search_in_engines_browser_origin, search_in_engines_domain) " +
                        " VALUES (?,?,?,?,?)");

        String encoded;
        String substring;
        String words;

        while (rs.next()) {
            encoded = rs.getString("word");
            substring = encoded.substring(0, encoded.contains("&") ? encoded.indexOf("&") : encoded.length() -1);

            //In case the string has been decoded already
            try {
                words = URLDecoder.decode(substring, "UTF-8"); }
            catch(Exception ex) {
                words = substring;
            }
            preparedStatement.setString(1, words);
            preparedStatement.setString(2, rs.getString("url_full"));
            preparedStatement.setString(3, user);
            preparedStatement.setString(4, getModuleName());
            preparedStatement.setString(5, rs.getString("url_domain"));
            preparedStatement.addBatch();

        }

        preparedStatement.executeBatch();
    }

    public String extractDomainFromFullUrlInSqliteQuery(String fullUrl, String newColumnName) {
        return "replace( SUBSTR( substr(" + fullUrl + ", instr(" + fullUrl+ ", '://')+3), 0, " +
                "instr(substr(" + fullUrl +", instr(" + fullUrl + ", '://')+3),'/')), 'www.', '') as " + newColumnName;
    }

    protected abstract String extractDateFromColumn(String oldColumn, String newColumn, String format);
    public abstract String getPathToBrowserHistory();
    public abstract String getHistoryFilename();
    public abstract String getLoginDataFilename();


}
