package main.pt.ipleiria.estg.dei.labcif.bhh.utils;

import main.pt.ipleiria.estg.dei.labcif.bhh.panels.ingestModulePanel.BrowserHistoryIngestModuleFactory;
import main.pt.ipleiria.estg.dei.labcif.bhh.panels.mainPanel.MainFrame;
import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.autopsy.ingest.IngestServices;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerBHH<T>{
    private org.sleuthkit.autopsy.coreutils.Logger autopsyLogger;
    private Logger standaloneLogger;

    public LoggerBHH(Class<T> origin) {
        try {
            this.autopsyLogger = org.sleuthkit.autopsy.coreutils.Logger.getLogger(origin.getName());
        } catch (NoClassDefFoundError ex) {
            standaloneLogger = Logger.getLogger(origin.getName());
        }
    }

    public void error(String message) {
        if (isAutopsyLogger()) {
            executeProductionLogger(Level.SEVERE, IngestMessage.MessageType.ERROR, message);
        } else if (isStandaloneLogger()) {
            MainFrame.getInstance().postMessage(message);
            standaloneLogger.log(Level.SEVERE, message);
        }
    }

    public void info(String message) {
        if (isAutopsyLogger()) {
            executeProductionLogger(Level.INFO, IngestMessage.MessageType.INFO, message);
        } else if (isStandaloneLogger()) {
            MainFrame.getInstance().postMessage(message);
            standaloneLogger.log(Level.INFO, message);
        }
    }

    public void warn(String message) {
        if (isAutopsyLogger()) {
            executeProductionLogger(Level.WARNING, IngestMessage.MessageType.WARNING, message);
        } else if (isStandaloneLogger()) {
            MainFrame.getInstance().postMessage(message);
            standaloneLogger.log(Level.WARNING, message);
        }
    }

    private void executeProductionLogger(Level level, IngestMessage.MessageType messageType, String message) {
        autopsyLogger.log(level, message);
        IngestMessage ingestMessage = IngestMessage.createMessage( messageType, BrowserHistoryIngestModuleFactory.getModuleName(),
                message);
        IngestServices.getInstance().postMessage(ingestMessage);
    }

    private boolean isAutopsyLogger() {
        return autopsyLogger != null;
    }

    private boolean isStandaloneLogger() {
        return standaloneLogger != null;
    }
}
