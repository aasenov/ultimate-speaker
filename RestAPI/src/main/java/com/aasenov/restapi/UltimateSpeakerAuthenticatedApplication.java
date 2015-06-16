package com.aasenov.restapi;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;

import com.aasenov.restapi.resources.FileResource;
import com.aasenov.restapi.resources.FilesResource;
import com.aasenov.restapi.resources.SearchResource;

public class UltimateSpeakerAuthenticatedApplication extends Application {

    public UltimateSpeakerAuthenticatedApplication() {
        setName("UltimateSpeaker RESTful application");
        setDescription("Receives RestAPI calls to operate with the system.");
        setOwner("Sofia University \"St. Kliment Ohridski\"");
        setAuthor("Asen Asenov");
    }

    @Override
    public Restlet createInboundRoot() {
        ChallengeAuthenticator authenticator = new ChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_BASIC,
                "Ultimate Speaker");
        UserVerifier verifier = new UserVerifier();
        authenticator.setVerifier(verifier);
        Router router = setupRouter();
        authenticator.setNext(router);
        return authenticator;
    }

    private Router setupRouter() {
        Router route = new Router();
        route.attach("/files", FilesResource.class);
        route.attach("/files/{hash}", FileResource.class);
        route.attach("/search", SearchResource.class);
        return route;
    }
}
