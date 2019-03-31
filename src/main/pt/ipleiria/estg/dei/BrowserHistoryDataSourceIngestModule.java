
package main.pt.ipleiria.estg.dei;

import main.pt.ipleiria.estg.dei.db.etl.DatabaseCreator;
import main.pt.ipleiria.estg.dei.model.browsers.Browser;
import main.pt.ipleiria.estg.dei.model.browsers.Chrome;
import org.sleuthkit.autopsy.casemodule.Case;
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
        DatabaseCreator.init(Case.getCurrentCase().getCaseDirectory());
        browsersToRun = new ArrayList<>();
        browsersToRun.add(new Chrome(context));

    }

    @Override
    public ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar) {
        progressBar.switchToDeterminate(3);

        browsersToRun.forEach(browser -> browser.run(dataSource));

        progressBar.switchToDeterminate(1);
        IngestMessage message = IngestMessage.createMessage( IngestMessage.MessageType.INFO, "browser History","Done");
        IngestServices.getInstance().postMessage(message);
        return IngestModule.ProcessResult.OK;
    }


}