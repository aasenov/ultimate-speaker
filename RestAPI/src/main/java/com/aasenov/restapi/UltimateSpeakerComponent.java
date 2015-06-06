package com.aasenov.restapi;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;

import com.aasenov.database.manager.DatabaseProvider;
import com.aasenov.helper.PathHelper;
import com.aasenov.restapi.managers.FileManager;
import com.aasenov.searchengine.provider.SearchManagerProvider;

public class UltimateSpeakerComponent extends Component {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(UltimateSpeakerComponent.class);

    public UltimateSpeakerComponent(int port) {
        setName("UltimateSpeaker RESTful Component");
        setDescription("Receives RestAPI calls to operate with the system.");
        setOwner("Sofia University \"St. Kliment Ohridski\"");
        setAuthor("Asen Asenov");

        getServers().add(new Server(Protocol.HTTP, port));
        UltimateSpeakerApplication app = new UltimateSpeakerApplication();
        getDefaultHost().attachDefault(app);
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

        // TODO - cleanup only when required.
        // clean DB on close
        DatabaseProvider.getDefaultManager().deleteAllTables();

        // delete temp file and folders
        for (String folderToDelete : new String[] { PathHelper.getJarContainingFolder() + "data",
                PathHelper.getJarContainingFolder() + "logs", FileManager.sOriginalFilesDir,
                FileManager.sParsedFilesDir, FileManager.sSpeechFilesDir }) {
            sLog.info("Deleting dir: " + folderToDelete);
            FileUtils.deleteDirectory(new File(folderToDelete));
        }
        File dbFile = new File(PathHelper.getJarContainingFolder() + "simple.db");
        if (dbFile.exists()) {
            sLog.info("Deleting file: " + dbFile.getName());
            dbFile.delete();
        }
    }
}
