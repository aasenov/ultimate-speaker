package com.aasenov.restapi;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.aasenov.restapi.resources.FileResource;
import com.aasenov.restapi.resources.FilesResource;
import com.aasenov.restapi.resources.SearchResource;
import com.aasenov.restapi.resources.TestResource;

public class UltimateSpeakerApplication extends Application {

    public UltimateSpeakerApplication() {
        setName("UltimateSpeaker RESTful application");
        setDescription("Receives RestAPI calls to operate with the system.");
        setOwner("Sofia University \"St. Kliment Ohridski\"");
        setAuthor("Asen Asenov");
    }

    @Override
    public Restlet createInboundRoot() {
        Router route = new Router();
        route.attach("/", TestResource.class);
        route.attach("/files", FilesResource.class);
        route.attach("/files/{hash}", FileResource.class);
        route.attach("/search", SearchResource.class);
        return route;
    }
}
