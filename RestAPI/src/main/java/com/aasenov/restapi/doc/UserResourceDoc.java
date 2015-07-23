package com.aasenov.restapi.doc;

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Status;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterInfo;
import org.restlet.ext.wadl.ParameterStyle;
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
        setDescription("Provide operation over users.");
    }

    @Override
    protected void describePost(MethodInfo info) {
        super.describePost(info);

        DocumentationInfo doc = new DocumentationInfo("Authenticate user.");
        doc.setTitle("Description");
        info.getDocumentations().add(doc);

        // describe parameters
        List<ParameterInfo> parameters = new ArrayList<ParameterInfo>();
        parameters.add(new ParameterInfo(PARAM_USER_MAIL, true, "xsi:string", ParameterStyle.QUERY,
                "Mail of user to authenticate"));
        parameters.add(new ParameterInfo(PARAM_PASSWORD, true, "xsi:string", ParameterStyle.QUERY,
                "Password to use for authentication"));
        if (info.getRequest() == null) {
            info.setRequest(new RequestInfo());
        }
        info.getRequest().setParameters(parameters);

        // responses
        info.getResponse().setDocumentation("If authentication was successful.");
        ResponseInfo errorResponse = new ResponseInfo("If some mandatory parameter or attribute is missing.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_BAD_REQUEST);
        info.getResponses().add(errorResponse);
        errorResponse = new ResponseInfo("In case of error during authentication.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_UNAUTHORIZED);
        info.getResponses().add(errorResponse);
    }

    @Override
    protected void describePut(MethodInfo info) {
        super.describePut(info);

        DocumentationInfo doc = new DocumentationInfo("Register user.");
        doc.setTitle("Description");
        info.getDocumentations().add(doc);

        // describe parameters
        List<ParameterInfo> parameters = new ArrayList<ParameterInfo>();
        parameters.add(new ParameterInfo(PARAM_USER_NAME, true, "xsi:string", ParameterStyle.QUERY,
                "Name of user to register"));
        parameters.add(new ParameterInfo(PARAM_USER_MAIL, true, "xsi:string", ParameterStyle.QUERY,
                "Mail of user to register. Should be unique for the system."));
        parameters.add(new ParameterInfo(PARAM_PASSWORD, true, "xsi:string", ParameterStyle.QUERY,
                "Password to use for register"));
        if (info.getRequest() == null) {
            info.setRequest(new RequestInfo());
        }
        info.getRequest().setParameters(parameters);

        // responses
        info.getResponse().setDocumentation("If registration was successful.");
        ResponseInfo errorResponse = new ResponseInfo("If some mandatory parameter or attribute is missing.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_BAD_REQUEST);
        info.getResponses().add(errorResponse);
        errorResponse = new ResponseInfo("In case of user with same mail already exists.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_CONFLICT);
        info.getResponses().add(errorResponse);
        errorResponse = new ResponseInfo("In case of error during request processing.");
        errorResponse.getStatuses().add(Status.SERVER_ERROR_INTERNAL);
        info.getResponses().add(errorResponse);
    }
}
