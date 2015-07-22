package com.aasenov.restapi;

import java.util.ArrayList;
import java.util.List;

import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.ext.wadl.ApplicationInfo;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ResourceInfo;
import org.restlet.ext.wadl.WadlApplication;
import org.restlet.ext.wadl.WadlDescribable;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;

import com.aasenov.restapi.resources.FileResource;
import com.aasenov.restapi.resources.FilesResource;
import com.aasenov.restapi.resources.SearchResource;

public class UltimateSpeakerAuthenticatedApplication extends WadlApplication implements WadlDescribable {

    public UltimateSpeakerAuthenticatedApplication() {
        setName("UltimateSpeaker RESTful authenticated application");
        setDescription("Receives RestAPI authenticated calls to operate with the system.");
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

    @Override
    public ResourceInfo getResourceInfo(ApplicationInfo app) {
        DocumentationInfo doc = new DocumentationInfo("Used to operate with objects, that require authentication.");
        doc.setTitle("UltimateSpeaker RESTful authenticated application");
        ResourceInfo info = new ResourceInfo(doc);

        // describe methods
        List<MethodInfo> methods = new ArrayList<MethodInfo>();
        MethodInfo methodGet = new MethodInfo();
        methodGet.setName(Method.GET);

        doc = new DocumentationInfo(
                "UltimateSpeaker RESTful authenticated application - for detail information click the URL from example.");
        doc.setTitle("Description");
        methodGet.getDocumentations().add(doc);

        doc = new DocumentationInfo("/management/?method=options");
        doc.setTitle("Example");
        methodGet.getDocumentations().add(doc);
        methods.add(methodGet);
        info.setMethods(methods);

        return info;
    }
}
