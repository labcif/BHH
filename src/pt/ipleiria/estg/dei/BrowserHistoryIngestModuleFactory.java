
package pt.ipleiria.estg.dei;


import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.ingest.*;

/**
 * IMPORTANT TIP: This sample ingest module factory directly implements
 * IngestModuleFactory. A practical alternative, recommended if you do not need
 * to provide implementations of all of the IngestModuleFactory methods, is to
 * extend the abstract class IngestModuleFactoryAdapter to get default
 * implementations of most of the IngestModuleFactory methods.
 */

@ServiceProvider(service = IngestModuleFactory.class) // Sample is discarded at runtime 
public class BrowserHistoryIngestModuleFactory extends IngestModuleFactoryAdapter {


    static String getModuleName() {
        return NbBundle.getMessage(BrowserHistoryIngestModuleFactory.class, "BrowserHistoryIngestModuleFactory.moduleName");
    }

    /**
     * @return The module family display name. Used to identify the module in user
     * interface components and log messages
     */
    @Override
    public String getModuleDisplayName() {
        return getModuleName();
    }

    @Override
    public String getModuleDescription() {
        return NbBundle.getMessage(BrowserHistoryIngestModuleFactory.class, "BrowserHistoryIngestModuleFactory.moduleDescription");
    }


    @Override
    public String getModuleVersionNumber() {
        return "1.0.0";
    }


    /**
     * Queries the factory to determine if it is capable of creating data source
     * ingest modules. If the module family does not include data source ingest
     * modules, the factory may extend IngestModuleFactoryAdapter to get an
     * implementation of this method that returns false.
     *
     * @return True if the factory can create data source ingest modules.
     */
    @Override
    public boolean isDataSourceIngestModuleFactory() {
        return true;
    }

    /**
     * @param settings The settings for the ingest job.
     *
     * @return A data source ingest module instance.
     */
    @Override
    public DataSourceIngestModule createDataSourceIngestModule(IngestModuleIngestJobSettings settings) {
        if (!(settings instanceof BrowserHistoryModuleIngestJobSettings)) {
            throw new IllegalArgumentException("Expected settings argument to be instanceof BrowserHistoryModuleIngestJobSettings");
        }
        return new BrowserHistoryDataSourceIngestModule((BrowserHistoryModuleIngestJobSettings) settings);
    }

    /**
     * Queries the factory to determine if it provides a user interface panel to
     * allow a user to change settings that are used by all instances of the
     * family of ingest modules the factory creates. For example, the Autopsy
     * core hash lookup ingest module factory provides a global settings panel
     * to import and create hash databases. The hash databases are then enabled
     * or disabled per ingest job using an ingest job settings panel. If the
     * module family does not have global settings, the factory may extend
     * IngestModuleFactoryAdapter to get an implementation of this method that
     * returns false.
     *
     * @return True if the factory provides a global settings panel.
     */
    @Override
    public boolean hasGlobalSettingsPanel() {
        return false;
    }

    /**
     * Gets the default per ingest job settings for instances of the family of
     * ingest modules the factory creates. For example, the Autopsy core hash
     * lookup ingest modules family uses hash databases imported or created
     * using its global settings panel. All of the hash databases are enabled by
     * default for an ingest job. If the module family does not have per ingest
     * job settings, the factory may extend IngestModuleFactoryAdapter to get an
     * implementation of this method that returns an instance of the
     * NoIngestModuleJobSettings class.
     *
     * @return The default ingest job settings.
     */
    @Override
    public IngestModuleIngestJobSettings getDefaultIngestJobSettings() {
        return new BrowserHistoryModuleIngestJobSettings();
    }

    /**
     * Queries the factory to determine if it provides user a interface panel to
     * allow a user to make per ingest job settings for instances of the family
     * of ingest modules the factory creates. For example, the Autopsy core hash
     * lookup ingest module factory provides an ingest job settings panels to
     * enable or disable hash databases per ingest job. If the module family
     * does not have per ingest job settings, the factory may extend
     * IngestModuleFactoryAdapter to get an implementation of this method that
     * returns false.
     *
     * @return True if the factory provides ingest job settings panels.
     */
    @Override
    public boolean hasIngestJobSettingsPanel() {
        return true;
    }

    /**
     * Gets a user interface panel that can be used to set per ingest job
     * settings for instances of the family of ingest modules the factory
     * creates. For example, the core hash lookup ingest module factory provides
     * an ingest job settings panel to enable or disable hash databases per
     * ingest job. If the module family does not have per ingest job settings,
     * the factory may extend IngestModuleFactoryAdapter to get an
     * implementation of this method that throws an
     * UnsupportedOperationException.
     *
     * @param settings Per ingest job settings to initialize the panel.
     *
     * @return An ingest job settings panel.
     */
    @Override
    public IngestModuleIngestJobSettingsPanel getIngestJobSettingsPanel(IngestModuleIngestJobSettings settings) {
        if (!(settings instanceof BrowserHistoryModuleIngestJobSettings)) {
            throw new IllegalArgumentException("Expected settings argument to be instanceof BrowserHistoryModuleIngestJobSettings");
        }
        return new BrowserHistoryModuleIngestJobSettingsPanel((BrowserHistoryModuleIngestJobSettings) settings);
    }

}
