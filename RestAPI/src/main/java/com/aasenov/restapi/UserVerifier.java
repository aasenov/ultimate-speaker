package com.aasenov.restapi;

import org.apache.log4j.Logger;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.security.Verifier;

import com.aasenov.database.objects.DatabaseTable;
import com.aasenov.database.objects.UserItem;

public class UserVerifier implements Verifier {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(UserVerifier.class);

    /**
     * Database table containing users.
     */
    private static DatabaseTable<UserItem> mUsersTable = new DatabaseTable<UserItem>(UserItem.DEFAULT_TABLE_NAME,
            new UserItem(null));

    @Override
    public int verify(Request req, Response resp) {
        ChallengeResponse challengeResponse = req.getChallengeResponse();
        if (challengeResponse == null) {
            return RESULT_MISSING;
        }

        String userLogonName = challengeResponse.getIdentifier();
        String userPass = new String(challengeResponse.getSecret());
        sLog.info("Supplied user name: " + userLogonName);

        boolean authenticated = false;
        UserItem user = mUsersTable.get(userLogonName);
        if (user != null) {
            if (user.getPassword().equals(userPass)) {
                authenticated = true;
            }
        } else {
            sLog.info(String.format("No user with name '%s' found", userLogonName));
        }

        if (authenticated) {
            sLog.info("User " + userLogonName + " authenticated.");
            req.getAttributes().put("UserObject", user);
            return RESULT_VALID;
        }

        return RESULT_INVALID;
    }
}
