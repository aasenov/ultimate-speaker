package com.aasenov.restapi;

import org.apache.log4j.Logger;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;

import com.aasenov.database.manager.DatabaseProvider;
import com.aasenov.helper.ConfigHelper;
import com.aasenov.helper.ConfigProperty;
import com.aasenov.restapi.managers.FileManager;
import com.aasenov.searchengine.provider.SearchManagerProvider;

public class UltimateSpeakerComponent extends Component {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(UltimateSpeakerComponent.class);

    public UltimateSpeakerComponent() {
        setName("UltimateSpeaker RESTful Component");
        setDescription("Receives RestAPI calls to operate with the system.");
        setOwner("Sofia University \"St. Kliment Ohridski\"");
        setAuthor("Asen Asenov");

        UltimateSpeakerApplication app = new UltimateSpeakerApplication();
        getDefaultHost().attachDefault(app);
    }

    @Override
    public synchronized void start() throws Exception {
        // reinitialize
        getServers().clear();
        int port = ConfigHelper.DEFAULT_REST_API_LISTEN_PORT;
        try {
            port = Integer.parseInt(ConfigHelper.getInstance().getConfigPropertyValue(ConfigProperty.RestAPIPort));
        } catch (NumberFormatException ex) {
            sLog.error(ex.getMessage(), ex);
        }
        getServers().add(new Server(Protocol.HTTP, port));

        // destroy static managers
        FileManager.destroy();
        DatabaseProvider.destroyManagers();
        SearchManagerProvider.destroyManagers();

        super.start();

        // initialize default search manager on startup to prevent first user to wait initialization.
        sLog.info(String.format("Starting %s Search", SearchManagerProvider.getEngineType()));
        SearchManagerProvider.getDefaultSearchManager().initialize();
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        // initialize default search manager on startup to prevent first user to wait initialization.
        sLog.info(String.format("Stopping %s Search", SearchManagerProvider.getEngineType()));
        SearchManagerProvider.getDefaultSearchManager().close();
    }

    /**
     * Clean all create files and DB records. Application should be stoppend in order to do this operation.
     */
    public void cleanup() {
        if (isStarted()) {
            sLog.error("Please stop the application before cleaning!");
            return;
        }

        // delete storage folders
        FileManager.getInstance().deleteStorage();

        // delete search storage folder
        SearchManagerProvider.getDefaultSearchManager().deleteStorageFolders();

        // clean DB on close
        DatabaseProvider.getDefaultManager().deleteAllTables();
    }
}
