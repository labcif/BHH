package main.pt.ipleiria.estg.dei.events;

import main.pt.ipleiria.estg.dei.BrowserHistoryReportModule;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModuleProgress;
import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.autopsy.ingest.IngestServices;

import java.io.File;

public class EtlObserver implements Observer {
    private DataSourceIngestModuleProgress progressBar;
    private static int amountOfTasks = 0;
    public EtlObserver(DataSourceIngestModuleProgress progressBar, int workAmount) {
        this.progressBar = progressBar;
        progressBar.switchToDeterminate(workAmount );
    }

    @Override
    public void update(String eventType) {
        progressBar.progress(amountOfTasks ++);
    }
}
