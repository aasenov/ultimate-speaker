import org.restlet.Server;
import org.restlet.data.Protocol;

import com.aasenov.restapi.UltimateSpeakerApplication;

public class Test {

    /**
     * Default application port to listen for requests
     */
    public static final int REST_PORT = 8181;

    public static void main(String[] args) throws Exception {
        // Create the HTTP server and listen on port 8182
        Server restServer = new Server(Protocol.HTTP, REST_PORT);
        restServer.setNext(new UltimateSpeakerApplication());
        restServer.start();

    }

}
