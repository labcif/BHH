package main.pt.ipleiria.estg.dei.labcif.bhh.utils;

import main.pt.ipleiria.estg.dei.labcif.bhh.panels.ingestModulePanel.BrowserHistoryIngestModuleFactory;
import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.autopsy.ingest.IngestServices;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerBHH<T>{
    private org.sleuthkit.autopsy.coreutils.Logger logger;
    private Logger loggerTests;

    public LoggerBHH(Class<T> origin) {
        try {
            this.logger = org.sleuthkit.autopsy.coreutils.Logger.getLogger(origin.getName());
        } catch (NoClassDefFoundError ex) {
            loggerTests = Logger.getLogger(origin.getName());
        }
    }

    public void error(String message) {
        if (isProductionLogger()) {
            executeProductionLogger(Level.SEVERE, IngestMessage.MessageType.ERROR, message);
        } else if (isTestLogger()) {
            loggerTests.log(Level.SEVERE, message);
        }
    }

    public void info(String message) {
        if (isProductionLogger()) {
            executeProductionLogger(Level.INFO, IngestMessage.MessageType.INFO, message);
        } else if (isTestLogger()) {
            loggerTests.log(Level.INFO, message);
        }
    }

    public void warn(String message) {
        if (isProductionLogger()) {
            executeProductionLogger(Level.WARNING, IngestMessage.MessageType.WARNING, message);
        } else if (isTestLogger()) {
            loggerTests.log(Level.WARNING, message);
        }
    }

    private void executeProductionLogger(Level level, IngestMessage.MessageType messageType, String message) {
        logger.log(level, message);
        IngestMessage ingestMessage = IngestMessage.createMessage( messageType, BrowserHistoryIngestModuleFactory.getModuleName(),
                message);
        IngestServices.getInstance().postMessage(ingestMessage);
    }

    public boolean isProductionLogger() {
        return logger != null;
    }

    public boolean isTestLogger() {
        return loggerTests != null;
    }
}
