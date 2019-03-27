
package main.pt.ipleiria.estg.dei;

import main.pt.ipleiria.estg.dei.db.DataWarehouseFactory;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.ingest.*;
import org.sleuthkit.datamodel.Content;


class BrowserHistoryDataSourceIngestModule implements DataSourceIngestModule {
    private Logger logger = Logger.getLogger(BrowserHistoryIngestModuleFactory.getModuleName());


    BrowserHistoryDataSourceIngestModule(BrowserHistoryModuleIngestJobSettings settings) {
    }

    @Override
    public void startUp(IngestJobContext context) {
    }

    @Override
    public ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar) {
        progressBar.switchToDeterminate(3);
        DataWarehouseFactory.run(Case.getCurrentCase().getCaseDirectory());//TODO: implement a event listener to tell status of process
        progressBar.switchToDeterminate(1);
        IngestMessage message = IngestMessage.createMessage( IngestMessage.MessageType.INFO, "browser History","Done");
        IngestServices.getInstance().postMessage(message);
        return IngestModule.ProcessResult.OK;
    }


}