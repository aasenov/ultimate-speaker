package com.aasenov.restapi;

import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;

import com.aasenov.database.manager.DatabaseProvider;

public class UltimateSpeakerComponent extends Component {
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
}
