package com.aasenov.restapi.resources;

import org.apache.log4j.Logger;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.aasenov.database.objects.DatabaseTable;
import com.aasenov.database.objects.UserItem;

/**
 * Use this resource for user creation and authentication.
 */
public class UserResource extends ServerResource {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(UserResource.class);
    /**
     * Database table containing users.
     */
    private static DatabaseTable<UserItem> mUsersTable = new DatabaseTable<UserItem>(UserItem.DEFAULT_TABLE_NAME,
            new UserItem(null));

    @Post
    public Representation authenticateUser(Representation entity) throws ResourceException {
        final Form form = new Form(entity);
        String userMail = form.getFirstValue("usermail");
        String userPass = form.getFirstValue("password");
        sLog.info("Supplied user mail: " + userMail);

        boolean authenticated = false;
        UserItem user = mUsersTable.get(userMail);
        if (user != null) {
            if (user.getPassword().equals(userPass)) {
                authenticated = true;
            }
        } else {
            sLog.info(String.format("No user with email '%s' found", userMail));
        }

        if (authenticated) {
            sLog.info("User " + userMail + " authenticated.");
            return new StringRepresentation("Authentication successful.", MediaType.TEXT_PLAIN);
        } else {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return new StringRepresentation("Authentication failed.", MediaType.TEXT_PLAIN);
        }
    }

    @Put
    public Representation registerUser(Representation entity) throws ResourceException {
        final Form form = new Form(entity);
        String userName = form.getFirstValue("username");
        String userMail = form.getFirstValue("usermail");
        String userPass = form.getFirstValue("password");
        sLog.info("Supplied registration user mail: " + userMail);

        // check for existance
        synchronized (mUsersTable) {
            if (mUsersTable.get(userMail) != null) {
                String message = "User with email '" + userMail + "' already exists.";
                setStatus(Status.CLIENT_ERROR_CONFLICT);
                sLog.info(message);
                return new StringRepresentation(message, MediaType.TEXT_PLAIN);
            } else {
                UserItem newUser = new UserItem(userName, userPass, userMail);
                mUsersTable.add(newUser);
            }

            if (mUsersTable.get(userMail) == null) {
                String message = "Unable to create user with email '" + userMail + "'. Please check the logs.";
                setStatus(Status.SERVER_ERROR_INTERNAL);
                sLog.info(message);
                return new StringRepresentation(message, MediaType.TEXT_PLAIN);
            }
        }

        return new StringRepresentation("User created.", MediaType.TEXT_PLAIN);
    }
}
