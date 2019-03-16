
package pt.ipleiria.estg.dei;

import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.services.Blackboard;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.ingest.*;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.Content;
import pt.ipleiria.estg.dei.db.GoogleChromeRepository;
import pt.ipleiria.estg.dei.model.GoogleChrome;
import pt.ipleiria.estg.dei.utils.Utils;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static pt.ipleiria.estg.dei.BrowserHistoryReportModule.ARTIFACT_TYPE_BLOCKED_HISTORY;
import static pt.ipleiria.estg.dei.BrowserHistoryReportModule.ARTIFACT_TYPE_BROWSER_HISTORY;


class BrowserHistoryDataSourceIngestModule implements DataSourceIngestModule {

    private Logger logger = Logger.getLogger(BrowserHistoryIngestModuleFactory.getModuleName());

    BrowserHistoryDataSourceIngestModule() {
    }

    @Override
    public void startUp(IngestJobContext context) {
    }

    @Override
    public ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar) {
        progressBar.switchToDeterminate(2);

        try {
            Blackboard blackboard = Case.getCurrentCaseThrows().getServices().getBlackboard();
            BlackboardAttribute.Type blackBoardAttributeType =
                    blackboard.getOrAddAttributeType("bytes",
                            BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.BYTE,
                            "Serialize class");


            BlackboardArtifact.Type urlsMostVisitedType =
                    blackboard.getOrAddArtifactType(ARTIFACT_TYPE_BROWSER_HISTORY, "Urls most visited");
            BlackboardArtifact artifactUrlsMostVisited = dataSource.newArtifact(urlsMostVisitedType.getTypeID());

            // Most Visited Sites
            List<GoogleChrome> mostVisitedSite = GoogleChromeRepository.INSTANCE.getMostVisitedSite();

            Collection<BlackboardAttribute> attributesOfMostVisited = mostVisitedSite
                    .stream()
                    .map(site ->
                            new BlackboardAttribute(
                                    blackBoardAttributeType,
                                    BrowserHistoryDataSourceIngestModule.class.getName(),
                                    Utils.convertToByte(site)))
                    .collect(Collectors.toList());

            artifactUrlsMostVisited.addAttributes(attributesOfMostVisited);
            blackboard.indexArtifact(artifactUrlsMostVisited);


            BlackboardArtifact.Type blockedUrlsType =
                    blackboard.getOrAddArtifactType(ARTIFACT_TYPE_BLOCKED_HISTORY, "Blocked Urls");
            BlackboardArtifact blackBoardArtifcatOfBlockedUrls = dataSource.newArtifact(blockedUrlsType.getTypeID());

            List<GoogleChrome> blokedSites = GoogleChromeRepository.INSTANCE.getDomainVisitedSites();
            Collection<BlackboardAttribute> attributeOfDomainVisited =
                    blokedSites
                            .stream()
                            .map(site ->
                                    new BlackboardAttribute(blackBoardAttributeType,
                                            BrowserHistoryDataSourceIngestModule.class.getName(),
                                            Utils.convertToByte(site)))
                            .collect(Collectors.toList());

            blackBoardArtifcatOfBlockedUrls.addAttributes(attributeOfDomainVisited);
        } catch (Exception ex) {
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