package main.pt.ipleiria.estg.dei.db;

import main.pt.ipleiria.estg.dei.db.etl.DatabaseCreator;
import main.pt.ipleiria.estg.dei.db.etl.Transformator;
import main.pt.ipleiria.estg.dei.utils.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DataWarehouseFactory {
    private static DataWarehouseFactory dataWarehouseFactory;
    private Logger<DataWarehouseFactory> logger = new Logger<>(DataWarehouseFactory.class);

    private DataWarehouseFactory(String databaseLocation) {
        DatabaseCreator.init(databaseLocation);
//        Extractor.run();
        Transformator.tranform();
    }

    public static void run(String databaseLocation) {
        if (isFirstExtraction()) {
            dataWarehouseFactory = new DataWarehouseFactory(databaseLocation);
        } else {
            run();
        }
    }
    private static boolean isFirstExtraction() {
        return dataWarehouseFactory == null;
    }

    private static void run() {
        throw new NotImplementedException();//TODO:
    }
}
