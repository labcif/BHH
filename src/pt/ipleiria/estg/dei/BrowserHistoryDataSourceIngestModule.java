
package pt.ipleiria.estg.dei;

import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.casemodule.services.Blackboard;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.ingest.*;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.TskCoreException;
import pt.ipleiria.estg.dei.db.GoogleChromeRepository;
import pt.ipleiria.estg.dei.model.GoogleChrome;
import pt.ipleiria.estg.dei.utils.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import static pt.ipleiria.estg.dei.BrowserHistoryReportModule.ARTIFACT_TYPE_BROWSER_HISTORY;
import static pt.ipleiria.estg.dei.BrowserHistoryReportModule.ARTIFACT_TYPE_BLOCKED_HISTORY;


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
            Blackboard blackboard = Case.getCurrentCaseThrows().getServices().getBlackboard();

           //Type
            BlackboardArtifact.Type artifactType = blackboard.getOrAddArtifactType(ARTIFACT_TYPE_BROWSER_HISTORY, "Urls most visited");
            BlackboardAttribute.Type attType = blackboard.getOrAddAttributeType("bytes", BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.BYTE, "Serialize class");
            BlackboardArtifact artifact = dataSource.newArtifact(artifactType.getTypeID());
            Collection<BlackboardAttribute> attributes = new ArrayList<>();

            // Most Visited Sites
            List<GoogleChrome> mostVisitedSite = GoogleChromeRepository.INSTANCE.getMostVisitedSite();
            mostVisitedSite.forEach(site ->{
                IngestServices
                        .getInstance()
                        .postMessage(
                                IngestMessage.createMessage(
                                        IngestMessage.MessageType.INFO,
                                        BrowserHistoryIngestModuleFactory.getModuleName(),
                                        site.toString()
                                )
                        );
                attributes.add(
                        new BlackboardAttribute(
                                attType,
                                BrowserHistoryDataSourceIngestModule.class.getName(),
                                Utils.convertToByte(site)));
            });

            artifact.addAttributes(attributes);
            blackboard.indexArtifact(artifact);//Indexing for key word search::TODO: this is not necessary, at least for now


            //Example
            BlackboardArtifact.Type artifactType2 = blackboard.getOrAddArtifactType(ARTIFACT_TYPE_BLOCKED_HISTORY, "Blocked Urls");
            BlackboardAttribute.Type attType2 = blackboard.getOrAddAttributeType("bytes", BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.BYTE, "Serialize class");
            BlackboardArtifact artifact2 = dataSource.newArtifact(artifactType2.getTypeID());
            Collection<BlackboardAttribute> attributes2 = new ArrayList<>();

            // Bloked websites
            List<GoogleChrome> blokedSites = GoogleChromeRepository.INSTANCE.getDomainVisitedSites();
            blokedSites.forEach(site ->{
                IngestServices
                        .getInstance()
                        .postMessage(
                                IngestMessage.createMessage(
                                        IngestMessage.MessageType.INFO,
                                        BrowserHistoryIngestModuleFactory.getModuleName(),
                                        site.toString()
                                )
                        );
                attributes2.add(
                        new BlackboardAttribute(
                                attType2,
                                BrowserHistoryDataSourceIngestModule.class.getName(),
                                Utils.convertToByte(site)));
            });
            artifact2.addAttributes(attributes2);


        } catch (ClassNotFoundException | SQLException | TskCoreException|Blackboard.BlackboardException
                | NoCurrentCaseException  ex) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return IngestModule.ProcessResult.OK;
    }

}