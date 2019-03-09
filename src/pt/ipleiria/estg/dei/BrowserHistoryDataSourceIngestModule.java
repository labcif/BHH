
package pt.ipleiria.estg.dei;

import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.ingest.*;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.TskCoreException;
import pt.ipleiria.estg.dei.db.GoogleChromeRepository;
import pt.ipleiria.estg.dei.model.BrowserEnum;
import pt.ipleiria.estg.dei.model.GoogleChrome;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


class BrowserHistoryDataSourceIngestModule implements DataSourceIngestModule {

    private Logger logger = Logger.getLogger(BrowserHistoryIngestModuleFactory.getModuleName());
    private IngestJobContext context = null;

    BrowserHistoryDataSourceIngestModule() {
    }

    @Override
    public void startUp(IngestJobContext context) throws IngestModuleException {
        this.context = context;
    }

    @Override
    public ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar) {
        // There are two tasks to do.
        progressBar.switchToDeterminate(2);

        try {
            List<GoogleChrome> mostVisitedSite = GoogleChromeRepository.INSTANCE.getMostVisitedSite(BrowserEnum.CHROME);
            mostVisitedSite.forEach(site ->
                    IngestServices
                            .getInstance()
                            .postMessage(
                                    IngestMessage.createMessage(
                                            IngestMessage.MessageType.INFO,
                                            BrowserHistoryIngestModuleFactory.getModuleName(),
                                            site.toString()
                                    )
                            )
            );

            BlackboardArtifact artifactIFH = dataSource.newArtifact(BlackboardArtifact.ARTIFACT_TYPE.TSK_INTERESTING_FILE_HIT);
            artifactIFH.addAttributes(new ArrayList<>());


        } catch (ClassNotFoundException | SQLException |TskCoreException ex) {
            IngestServices
                    .getInstance()
                    .postMessage(
                            IngestMessage
                                    .createMessage(
                                            IngestMessage.MessageType.ERROR,
                                            BrowserHistoryIngestModuleFactory.getModuleName(),
                                            ex.getMessage()));
            logger.log(Level.SEVERE, "Failed to execute query: " +ex.getMessage(), ex);
            return IngestModule.ProcessResult.ERROR;
        }

        return IngestModule.ProcessResult.OK;
    }
}