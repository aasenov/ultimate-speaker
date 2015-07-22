package com.aasenov.restapi.resources;

import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

public class DocumentationResource extends WadlServerResource {

    @Get("html")
    public Representation getDocumentation() {
        getResponse().redirectPermanent("/?method=options");
        return new StringRepresentation("You will be redirected.");
    }
}
