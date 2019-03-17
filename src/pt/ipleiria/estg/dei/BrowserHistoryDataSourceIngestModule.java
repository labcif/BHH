
package pt.ipleiria.estg.dei;

import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.services.Blackboard;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.ingest.*;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.Content;
import pt.ipleiria.estg.dei.db.GoogleChromeRepository;
import pt.ipleiria.estg.dei.dtos.RelativeFrequencyBrowser;
import pt.ipleiria.estg.dei.model.GoogleChrome;
import pt.ipleiria.estg.dei.utils.Utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static pt.ipleiria.estg.dei.BrowserHistoryReportModule.*;


class BrowserHistoryDataSourceIngestModule implements DataSourceIngestModule {

    private Logger logger = Logger.getLogger(BrowserHistoryIngestModuleFactory.getModuleName());
    private final boolean localDisk;
    private final boolean file;

    BrowserHistoryDataSourceIngestModule(BrowserHistoryModuleIngestJobSettings settings) {
        this.localDisk = settings.localDisk();
        this.file = settings.file();
    }

    @Override
    public void startUp(IngestJobContext context) {
    }

    @Override
    public ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar) {
        progressBar.switchToDeterminate(3);

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
            //blackboard.indexArtifact(artifactUrlsMostVisited);
            progressBar.progress(1);

            BlackboardArtifact.Type blockedUrlsType =
                    blackboard.getOrAddArtifactType(ARTIFACT_TYPE_BLOCKED_HISTORY, "Blocked Urls");
            BlackboardArtifact blackBoardArtifcatOfBlockedUrls = dataSource.newArtifact(blockedUrlsType.getTypeID());

            List<GoogleChrome> blokedSites = GoogleChromeRepository.INSTANCE.getBlockedSitesVisited();
            Collection<BlackboardAttribute> attributeOfDomainVisited =
                    blokedSites
                            .stream()
                            .map(site ->
                                    new BlackboardAttribute(blackBoardAttributeType,
                                            BrowserHistoryDataSourceIngestModule.class.getName(),
                                            Utils.convertToByte(site)))
                            .collect(Collectors.toList());

            blackBoardArtifcatOfBlockedUrls.addAttributes(attributeOfDomainVisited);
            progressBar.progress(1);
            List<String> wordsFromGoogleEngine = GoogleChromeRepository.INSTANCE.getWordsFromGoogleEngine();
            BlackboardArtifact.Type wordSearchInGoogle =
                    blackboard.getOrAddArtifactType(ARTIFACT_TYPE_WORDS_GOOGLE_ENGINE, "Words Search in Google Engine");
            BlackboardArtifact blackboardArtifactWordSearchInGoogle = dataSource.newArtifact(wordSearchInGoogle.getTypeID());

            Collection<BlackboardAttribute> attributesOfWordInGoogleEngine = wordsFromGoogleEngine.stream()
                    .map(word ->
                            new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_NAME,
                                    BrowserHistoryDataSourceIngestModule.class.getName(),
                                    word))
                    .collect(Collectors.toList());
            blackboardArtifactWordSearchInGoogle.addAttributes(attributesOfWordInGoogleEngine);
            progressBar.progress(1);

            BlackboardArtifact.Type relativeFrequencyType =
                    blackboard.getOrAddArtifactType(ARTIFACT_TYPE_FREQUENCY_HISTORY, "Frequency Domain");
            BlackboardArtifact artifactFrequencyDomain = dataSource.newArtifact(relativeFrequencyType.getTypeID());

            HashMap<String, List<GoogleChrome>> domainsByFrequencyPerDay = GoogleChromeRepository.INSTANCE.getDomainsByFrequencyPerDay();
            float daysDatabaseWasActive = GoogleChromeRepository.INSTANCE.getDaysDatabaseWasActive();

            Collection<BlackboardAttribute> attributesFrequencyDomainPerDay =
                    domainsByFrequencyPerDay.entrySet().stream()
                            .map(stringListEntry -> {
                                String key = stringListEntry.getKey();
                                int daysVisited = stringListEntry.getValue().size();
                                RelativeFrequencyBrowser relativeFrequencyBrowser =
                                        new RelativeFrequencyBrowser(key, daysVisited, daysDatabaseWasActive);
                                return new BlackboardAttribute(blackBoardAttributeType,
                                        BrowserHistoryDataSourceIngestModule.class.getName(),
                                        Utils.convertToByte(relativeFrequencyBrowser));
                            }).collect(Collectors.toList());

            artifactFrequencyDomain.addAttributes(attributesFrequencyDomainPerDay);

        } catch (Exception ex) {
            IngestServices
                    .getInstance()
                    .postMessage(
                            IngestMessage
                                    .createMessage(
                                            IngestMessage.MessageType.ERROR,
                                            BrowserHistoryIngestModuleFactory.getModuleName(),
                                            ex.getMessage()));
            logger.log(Level.SEVERE, "Failed to execute query: " + ex.getMessage(), ex);
            return IngestModule.ProcessResult.ERROR;
        }

        return IngestModule.ProcessResult.OK;
    }


}