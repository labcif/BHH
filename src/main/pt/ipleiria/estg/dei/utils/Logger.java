package main.pt.ipleiria.estg.dei.utils;

import main.pt.ipleiria.estg.dei.BrowserHistoryIngestModuleFactory;
import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.autopsy.ingest.IngestServices;

import java.util.logging.Level;

public class Logger <T>{
    private org.sleuthkit.autopsy.coreutils.Logger logger;

    public Logger(Class<T> origin) {
        this.logger = org.sleuthkit.autopsy.coreutils.Logger.getLogger(origin.getName());
    }

    public void error(String message) {
        logger.log(Level.SEVERE, message);
        IngestMessage ingestMessage = IngestMessage.createMessage( IngestMessage.MessageType.ERROR, BrowserHistoryIngestModuleFactory.getModuleName(),
                message);
        IngestServices.getInstance().postMessage(ingestMessage);
    }

    public void info(String message) {
        logger.log(Level.INFO, message);
        IngestMessage ingestMessage = IngestMessage.createMessage( IngestMessage.MessageType.INFO, BrowserHistoryIngestModuleFactory.getModuleName(),
                message);
        IngestServices.getInstance().postMessage(ingestMessage);
    }

    public void warn(String message) {
        logger.log(Level.WARNING, message);
        IngestMessage ingestMessage = IngestMessage.createMessage( IngestMessage.MessageType.WARNING, BrowserHistoryIngestModuleFactory.getModuleName(),
                message);
        IngestServices.getInstance().postMessage(ingestMessage);
    }
}
