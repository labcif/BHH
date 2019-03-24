package main.pt.ipleiria.estg.dei.utils;

import main.pt.ipleiria.estg.dei.db.DataWarehouseFactory;

import java.util.logging.Level;

public class Logger <T>{
    private org.sleuthkit.autopsy.coreutils.Logger logger;

    public Logger(Class<T> origin) {
        this.logger = org.sleuthkit.autopsy.coreutils.Logger.getLogger(origin.getName());
    }

    public void error(String message) {
        logger.log(Level.SEVERE, message);
    }

    public void info(String message) {
        logger.log(Level.INFO, message);
    }

    public void warn(String message) {
        logger.log(Level.WARNING, message);
    }
}
