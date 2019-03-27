package main.pt.ipleiria.estg.dei.db;

import main.pt.ipleiria.estg.dei.db.etl.DatabaseCreator;
import main.pt.ipleiria.estg.dei.db.etl.Extractor;
import main.pt.ipleiria.estg.dei.db.etl.Transformator;
import main.pt.ipleiria.estg.dei.utils.Logger;

public class DataWarehouseFactory {
    private Logger<DataWarehouseFactory> logger = new Logger<>(DataWarehouseFactory.class);

    private DataWarehouseFactory(String databaseLocation) {
        logger.info("Initiating database construction");
        DatabaseCreator.init(databaseLocation);
        logger.info("Database construction finished");
        logger.info("Initiating extraction");
        Extractor.run();
        logger.info("Extraction finished");
        logger.info("Initiating transformation");
        Transformator.tranform();
        logger.info("Transformation finished");
    }

    public static void run(String databaseLocation) {
        //Each time we run the ingest module will get a total extraction.
        new DataWarehouseFactory(databaseLocation);
    }

}
