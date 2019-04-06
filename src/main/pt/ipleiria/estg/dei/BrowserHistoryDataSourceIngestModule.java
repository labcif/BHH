
package main.pt.ipleiria.estg.dei;

import main.pt.ipleiria.estg.dei.db.etl.DatabaseCreator;
import main.pt.ipleiria.estg.dei.events.EtlObserver;
import main.pt.ipleiria.estg.dei.model.browsers.Browser;
import main.pt.ipleiria.estg.dei.model.browsers.Chrome;
import main.pt.ipleiria.estg.dei.model.browsers.Firefox;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.ingest.*;
import org.sleuthkit.datamodel.Content;

import java.util.ArrayList;
import java.util.List;


class BrowserHistoryDataSourceIngestModule implements DataSourceIngestModule {
    private Logger logger = Logger.getLogger(BrowserHistoryIngestModuleFactory.getModuleName());
    private List<Browser> browsersToRun;


    BrowserHistoryDataSourceIngestModule(BrowserHistoryModuleIngestJobSettings settings) {
    }

    @Override
    public void startUp(IngestJobContext context) {
        DatabaseCreator.init();
        browsersToRun = new ArrayList<>();
        browsersToRun.add(new Chrome(context));
        browsersToRun.add(new Firefox(context));

    }

    @Override
    public ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar) {

        browsersToRun.forEach(browser -> browser.events.subscribe("etl_process", new EtlObserver(progressBar, browsersToRun.size())));
        browsersToRun.forEach(browser -> {
                                            browser.run(dataSource);
                                            browser.events.notify("etl_process");
                                        });


        IngestMessage message = IngestMessage.createMessage( IngestMessage.MessageType.INFO, "browser History","Done");
        IngestServices.getInstance().postMessage(message);
        return IngestModule.ProcessResult.OK;
    }
}