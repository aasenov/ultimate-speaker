package com.aasenov.restapi;

import org.apache.log4j.Logger;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;

import com.aasenov.database.manager.DatabaseProvider;
import com.aasenov.searchengine.provider.SearchManagerProvider;

public class UltimateSpeakerComponent extends Component {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(UltimateSpeakerComponent.class);

    /**
     * Default application port to listen for requests
     */
    public static final int REST_PORT = 8181;

    public UltimateSpeakerComponent() {
        setName("UltimateSpeaker RESTful Component");
        setDescription("Receives RestAPI calls to operate with the system.");
        setOwner("Sofia University \"St. Kliment Ohridski\"");
        setAuthor("Asen Asenov");

        getServers().add(new Server(Protocol.HTTP, REST_PORT));
        getDefaultHost().attachDefault(new UltimateSpeakerApplication());

        // clean DB on start
        DatabaseProvider.getDefaultManager().deleteAllTables();
    }

    @Override
    public synchronized void start() throws Exception {
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

}
