package main.pt.ipleiria.estg.dei.labcif.bhh.events;


import main.pt.ipleiria.estg.dei.labcif.bhh.modules.Module;
import main.pt.ipleiria.estg.dei.labcif.bhh.utils.LoggerBHH;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModuleProgress;
import org.sleuthkit.datamodel.Content;

import java.util.List;

public class IngestModuleProgress {
    private static DataSourceIngestModuleProgress progressBar;
    private static int currentWork;
    private static IngestModuleProgress instance;
    private static int counter;
    private IngestModuleProgress() {
        counter = 0;
    }
    private LoggerBHH<IngestModuleProgress> loggerBHH = new LoggerBHH<>(IngestModuleProgress.class);

    public void init(Content dataSource, List<Module> modulesToRun, DataSourceIngestModuleProgress progress){
        progressBar = progress;
        currentWork = getTotalWork(dataSource, modulesToRun);
        progressBar.switchToDeterminate(currentWork );
    }

    private int getTotalWork(Content dataSource, List<Module> modulesToRun) {
        return 240;//TODO: Find a way to get number of users in the machine and multiply it by modulesToRun.size()*2 (process etl)
    }

    public void incrementProgress(String user, String module, String etlProcess) {
        if (progressBar == null || counter > 240) {
            return;
        }
        counter++;
        progressBar.progress( user + " - " + module + " - "+ etlProcess, counter);
    }

    public void finishProgress() {
        progressBar.switchToDeterminate(1);
        progressBar = null;
        currentWork = 0;
        instance = null;
        counter = 0;
        loggerBHH.info("Ingest module has finished");
    }

    public static IngestModuleProgress getInstance() {
        if (instance == null) {
            instance = new IngestModuleProgress();
        }
        return instance;
    }


}
