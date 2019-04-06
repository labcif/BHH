package main.pt.ipleiria.estg.dei.model.browsers;

import javafx.scene.control.ProgressBar;
import main.pt.ipleiria.estg.dei.events.EventManager;
import main.pt.ipleiria.estg.dei.exceptions.ExtractionException;
import main.pt.ipleiria.estg.dei.utils.Logger;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.datamodel.Content;


public abstract class Browser extends Data {
    private Case currentCase;
    private Logger<Browser> logger = new Logger<>(Browser.class);

    public EventManager events;

    protected IngestJobContext context;
    boolean dataFound;
    Content dataSource;

    protected Browser(IngestJobContext context) {
        try {
            currentCase = Case.getCurrentCaseThrows();
            this.context = context;
            this.events = new EventManager("etl_process" );

        } catch (NoCurrentCaseException e) {
            logger.error("Case couldn't be find. So state couldn't be initialized");
            throw new ExtractionException("Case couldn't be find. So state couldn't be initialized");
        }
    }

    public abstract void runHistory();
    public abstract void run(Content dataSource);
    public abstract String getBrowserName();

    public boolean isDataFound() {
        return dataFound;
    }
}
