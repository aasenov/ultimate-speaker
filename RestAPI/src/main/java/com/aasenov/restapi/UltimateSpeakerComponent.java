package com.aasenov.restapi;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.service.CorsService;
import org.restlet.util.Series;

import com.aasenov.database.provider.DatabaseProvider;
import com.aasenov.helper.ConfigHelper;
import com.aasenov.helper.ConfigProperty;
import com.aasenov.helper.PathHelper;
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

        UltimateSpeakerBasicApplication app = new UltimateSpeakerBasicApplication();
        getDefaultHost().attachDefault(app);

        // Enable CORS
        CorsService corsService = new CorsService();
        corsService.setAllowedOrigins(new HashSet<String>(Arrays.asList("*")));
        corsService.setAllowedCredentials(true); // allow authentication
        corsService.setSkippingResourceForCorsOptions(true); // stop options method to be processed by other resources.
        app.getServices().add(corsService);
    }

    @Override
    public synchronized void start() throws Exception {
        sLog.info("Starting UltimateSpeaker components.");

        // reinitialize
        sLog.info("Clean static values and server configurations.");
        getServers().clear();
        int port = ConfigHelper.DEFAULT_REST_API_LISTEN_PORT;
        try {
            port = Integer.parseInt(ConfigHelper.getInstance().getConfigPropertyValue(ConfigProperty.RestAPIPort));
        } catch (NumberFormatException ex) {
            sLog.error(ex.getMessage(), ex);
        }
        Server server = getServers().add(Protocol.HTTPS, port);

        // set https specific parameters
        Series<Parameter> parameters = server.getContext().getParameters();
        parameters.add("sslContextFactory", "org.restlet.engine.ssl.DefaultSslContextFactory");
        parameters.add("keyStorePath",
                new File(PathHelper.getJarContainingFolder(), "UltimateSpeaker.jks").getAbsolutePath());
        parameters.add("keyStorePassword", "UltimateSpeakerStorePass123");
        parameters.add("keyPassword", "UltimateSpeakerPass123");
        parameters.add("keyStoreType", "JKS");

        // destroy static managers
        FileManager.destroy();
        DatabaseProvider.destroyManagers();
        SearchManagerProvider.destroyManagers();

        super.start();

        // initialize default search manager on startup to prevent first user to wait initialization.
        sLog.info(String.format("Starting %s Search", SearchManagerProvider.getEngineType()));
        SearchManagerProvider.getDefaultSearchManager().initialize();

        sLog.info("UltimateSpeaker components started successfully.");
    }

    @Override
    public synchronized void stop() throws Exception {
        sLog.info("Stopping UltimateSpeaker components.");
        super.stop();
        // initialize default search manager on startup to prevent first user to wait initialization.
        sLog.info(String.format("Stopping %s Search", SearchManagerProvider.getEngineType()));
        SearchManagerProvider.getDefaultSearchManager().close();

        sLog.info("UltimateSpeaker components stopped successfully.");
    }

    /**
     * Clean all create files and DB records. Application should be stoppend in order to do this operation.
     */
    public void cleanup() {
        sLog.info("Cleaning UltimateSpeaker components.");

        if (isStarted()) {
            sLog.error("Please stop the application before cleaning!");
            return;
        }

        // delete storage folders
        FileManager.getInstance().deleteStorage();

        // delete search storage folder
        SearchManagerProvider.getDefaultSearchManager().deleteStorageFolders();

        // clean DB on close
        DatabaseProvider.getDefaultManager().deleteAllTableContents();

        sLog.info("UltimateSpeaker components cleaned up successfully.");
    }
}
