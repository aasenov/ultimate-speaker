package com.aasenov.restapi;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

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
        route.attachDefault(TestResource.class);
        return route;
    }
}
