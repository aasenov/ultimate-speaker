package com.aasenov.restapi.resources;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class TestResource extends ServerResource {

    @Get
    @Override
    public String toString() {
        return "This is test resource";
    }
}
