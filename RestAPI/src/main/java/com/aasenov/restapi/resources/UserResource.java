package com.aasenov.restapi.resources;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

import com.aasenov.database.objects.SpeechSettings;
import com.aasenov.database.objects.UserItem;
import com.aasenov.restapi.managers.UserManager;
import com.aasenov.restapi.mapper.MapperProvider;
import com.aasenov.restapi.mapper.ObjectMapper;

/**
 * Use this resource for user creation and authentication.
 */
public class UserResource extends WadlServerResource {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(UserResource.class);

    /**
     * Parameter containing type of file to download.
     */
    protected static final String PARAM_SET_DEFAULT = "setDefaults";

    @Put("json|xml:json|xml")
    public Representation updateSettings(Representation entity) throws ResourceException {
        sLog.info("Request for settings update received!");

        Parameter setDefault = getQuery().getFirst(PARAM_SET_DEFAULT);

        // update settings
        String userID = getRequest().getChallengeResponse().getIdentifier();
        try {

            // get settings to update
            SpeechSettings settings = null;
            ObjectMapper mapper = null;
            if (setDefault == null) {
                mapper = MapperProvider.getMapper(entity);
                settings = mapper.fromRepresentation(entity, SpeechSettings.class);
            } else {
                mapper = MapperProvider.getMapper(getRequest());
                settings = new SpeechSettings();
            }

            UserItem user = UserManager.getInstance().getUser(userID);
            user.setSpeechSettings(settings);
            UserManager.getInstance().updateUser(user);

            sLog.info(String.format("Settings for user'%s' successfully updated to '%s'.", userID, settings));
            return mapper.getRepresentation(user);
        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
            sLog.error(e.getMessage(), e);
            return new StringRepresentation(e.getMessage(), MediaType.TEXT_PLAIN);
        }
    }
}
