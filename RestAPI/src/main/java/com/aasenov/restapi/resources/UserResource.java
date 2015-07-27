package com.aasenov.restapi.resources;

import org.apache.log4j.Logger;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

import com.aasenov.database.objects.DatabaseTable;
import com.aasenov.database.objects.UserItem;

/**
 * Use this resource for user creation and authentication.
 */
public class UserResource extends WadlServerResource {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(UserResource.class);

    /**
     * Database table containing users.
     */
    private static DatabaseTable<UserItem> mUsersTable = new DatabaseTable<UserItem>(UserItem.DEFAULT_TABLE_NAME,
            new UserItem(null));

    /**
     * Parameter containing user name.
     */
    protected static final String PARAM_USER_NAME = "username";

    /**
     * Parameter containing user mail address.
     */
    protected static final String PARAM_USER_MAIL = "usermail";

    /**
     * Parameter containing user password.
     */
    protected static final String PARAM_PASSWORD = "password";

    @Post("form:txt")
    public Representation authenticateUser(Representation entity) throws ResourceException {
        final Form form = new Form(entity);
        String userMail = form.getFirstValue(PARAM_USER_MAIL);
        String userPass = form.getFirstValue(PARAM_PASSWORD);

        if (userMail == null || userMail.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("No user mail.", MediaType.TEXT_PLAIN);
        }

        if (userPass == null || userPass.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("No user password.", MediaType.TEXT_PLAIN);
        }

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

    @Put("form:txt")
    public Representation registerUser(Representation entity) throws ResourceException {
        final Form form = new Form(entity);
        String userName = form.getFirstValue(PARAM_USER_NAME);
        String userMail = form.getFirstValue(PARAM_USER_MAIL);
        String userPass = form.getFirstValue(PARAM_PASSWORD);

        if (userName == null || userName.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("No user name.", MediaType.TEXT_PLAIN);
        }

        if (userMail == null || userMail.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("No user mail.", MediaType.TEXT_PLAIN);
        }

        if (userPass == null || userPass.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("No user password.", MediaType.TEXT_PLAIN);
        }

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
