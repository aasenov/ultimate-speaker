package com.aasenov.restapi;

import org.restlet.Restlet;
import org.restlet.ext.wadl.WadlApplication;
import org.restlet.routing.Router;
import org.restlet.routing.Template;

import com.aasenov.restapi.doc.DocumentationResourceDoc;
import com.aasenov.restapi.doc.UsersResourceDoc;

public class UltimateSpeakerBasicApplication extends WadlApplication {

    public UltimateSpeakerBasicApplication() {
        setName("UltimateSpeaker RESTful application");
        setDescription("Receives RestAPI calls to operate with the system.");
        setOwner("Sofia University \"St. Kliment Ohridski\"");
        setAuthor("Asen Asenov");
    }

    @Override
    public Restlet createInboundRoot() {
        Router route = new Router();
        route.attach("/users", UsersResourceDoc.class);
        route.attach("/api-docs", DocumentationResourceDoc.class);
        route.attach("/management", new UltimateSpeakerAuthenticatedApplication()).setMatchingMode(
                Template.MODE_STARTS_WITH);
        return route;
    }
}
