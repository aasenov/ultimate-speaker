package com.aasenov.restapi.resources;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * Use this resource for user creation and authentication.
 */
public class UserResource extends ServerResource {

    @Get
    @Override
    public String toString() {
        return "This is test resource";
    }
}
