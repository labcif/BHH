
/*
 * Sample module in the public domain.  Feel free to use this as a template
 * for your modules.
 * 
 *  Contact: Brian Carrier [carrier <at> sleuthkit [dot] org]
 *
 *  This is free and unencumbered software released into the public domain.
 *  
 *  Anyone is free to copy, modify, publish, use, compile, sell, or
 *  distribute this software, either in source code form or as a compiled
 *  binary, for any purpose, commercial or non-commercial, and by any
 *  means.
 *  
 *  In jurisdictions that recognize copyright laws, the author or authors
 *  of this software dedicate any and all copyright interest in the
 *  software to the public domain. We make this dedication for the benefit
 *  of the public at large and to the detriment of our heirs and
 *  successors. We intend this dedication to be an overt act of
 *  relinquishment in perpetuity of all present and future rights to this
 *  software under copyright law.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 *  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE. 
 */
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