package com.aasenov.restapi;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.aasenov.restapi.resources.UserResource;

public class UltimateSpeakerBasicApplication extends Application {

    public UltimateSpeakerBasicApplication() {
        setName("UltimateSpeaker RESTful application");
        setDescription("Receives RestAPI calls to operate with the system.");
        setOwner("Sofia University \"St. Kliment Ohridski\"");
        setAuthor("Asen Asenov");
    }

    @Override
    public Restlet createInboundRoot() {
        Router route = new Router();
        route.attach("/users", UserResource.class);
        route.attachDefault(new UltimateSpeakerAuthenticatedApplication());
        return route;
    }
}
