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

import com.aasenov.restapi.resources.FilesResource;

/**
 * This class adds additional description to methods, provided by {@link FilesResource} class.
 */
public class FilesResourceDoc extends FilesResource {
    public FilesResourceDoc() {
        super();
        setName("Files resource");
        setDescription("Provide operation over multiple files.");
    }

    @Override
    protected void describeGet(MethodInfo info) {
        super.describeGet(info);

        DocumentationInfo doc = new DocumentationInfo("List files for authenticated user.");
        doc.setTitle("Description");
        info.getDocumentations().add(doc);

        // describe parameters
        List<ParameterInfo> parameters = new ArrayList<ParameterInfo>();
        parameters.add(new ParameterInfo("Authorization", true, "xsi:string", ParameterStyle.HEADER,
                "HTTP Basic Authentication."));
        ParameterInfo paramToAdd = new ParameterInfo(PARAM_START, false, "xsi:integer", ParameterStyle.QUERY,
                "Start point from where file listing should begin.");
        paramToAdd.setDefaultValue("0");
        parameters.add(paramToAdd);
        paramToAdd = new ParameterInfo(PARAM_COUNT, false, "xsi:integer", ParameterStyle.QUERY,
                "Number of files to return.");
        paramToAdd.setDefaultValue(Integer.toString(DEFAULT_PAGE_SIZE));
        parameters.add(paramToAdd);
        if (info.getRequest() == null) {
            info.setRequest(new RequestInfo());
        }
        info.getRequest().setParameters(parameters);

        // responses
        info.getResponse().setDocumentation("If listing was successful.");
        for (RepresentationInfo repr : info.getResponse().getRepresentations()) {
            if (repr.getMediaType() == MediaType.APPLICATION_JSON) {
                repr.setDocumentation("JSON serialized FileItemsList object, containing all requested files.");
            } else if (repr.getMediaType() == MediaType.APPLICATION_XML || repr.getMediaType() == MediaType.TEXT_XML) {
                repr.setDocumentation("XML serialized FileItemsList object, containing all requested files.");
            }
        }
        ResponseInfo errorResponse = new ResponseInfo("In case of error during authentication.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_UNAUTHORIZED);
        info.getResponses().add(errorResponse);
        errorResponse = new ResponseInfo("In case of error during request processing.");
        errorResponse.getStatuses().add(Status.SERVER_ERROR_INTERNAL);
        info.getResponses().add(errorResponse);
    }

    @Override
    protected void describePost(MethodInfo info) {
        super.describePost(info);

        DocumentationInfo doc = new DocumentationInfo("Upload files.");
        doc.setTitle("Description");
        info.getDocumentations().add(doc);

        // responses
        info.getResponse().setDocumentation("If upload was successful.");
        for (RepresentationInfo repr : info.getResponse().getRepresentations()) {
            if (repr.getMediaType() == MediaType.APPLICATION_JSON) {
                repr.setDocumentation("JSON serialized list of String, containing IDs of uploaded files.");
            }
        }
        ResponseInfo errorResponse = new ResponseInfo("In case of error during authentication.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_UNAUTHORIZED);
        info.getResponses().add(errorResponse);
        errorResponse = new ResponseInfo("If some mandatory parameter or attribute is missing.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_BAD_REQUEST);
        info.getResponses().add(errorResponse);
        errorResponse = new ResponseInfo("If file already exist for current user.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_CONFLICT);
        info.getResponses().add(errorResponse);
        errorResponse = new ResponseInfo("In case of error during request processing.");
        errorResponse.getStatuses().add(Status.SERVER_ERROR_INTERNAL);
        info.getResponses().add(errorResponse);
    }

}
