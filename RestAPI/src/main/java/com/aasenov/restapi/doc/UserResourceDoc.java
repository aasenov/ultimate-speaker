package com.aasenov.restapi.doc;

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.ext.wadl.RequestInfo;
import org.restlet.ext.wadl.ResponseInfo;

import com.aasenov.restapi.resources.UserResource;

/**
 * This class adds additional description to methods, provided by {@link UserResource} class.
 */
public class UserResourceDoc extends UserResource {
    public UserResourceDoc() {
        super();
        setName("User resource");
        setDescription("Provide operation over particular user.");
    }

    @Override
    protected void describePut(MethodInfo info) {
        super.describePut(info);

        DocumentationInfo doc = new DocumentationInfo("Update settings for user.");
        doc.setTitle("Description");
        info.getDocumentations().add(doc);

        // describe parameters
        List<ParameterInfo> parameters = new ArrayList<ParameterInfo>();
        parameters.add(new ParameterInfo(PARAM_SET_DEFAULT, false, "xsi:string", ParameterStyle.QUERY,
                "Set if you want to reset user settings to default."));
        if (info.getRequest() == null) {
            info.setRequest(new RequestInfo());
        }
        info.getRequest().setParameters(parameters);

        // responses
        info.getResponse().setDocumentation("If settings updating was successful.");
        for (RepresentationInfo repr : info.getResponse().getRepresentations()) {
            if (repr.getMediaType() == MediaType.APPLICATION_JSON) {
                repr.setDocumentation("JSON serialized UserItem object.");
            } else if (repr.getMediaType() == MediaType.APPLICATION_XML || repr.getMediaType() == MediaType.TEXT_XML) {
                repr.setDocumentation("XML serialized UserItem object.");
            }
        }

        ResponseInfo errorResponse = new ResponseInfo("In case of error during updating.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_UNAUTHORIZED);
        info.getResponses().add(errorResponse);
    }

}
