package com.aasenov.restapi.doc;

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.OptionInfo;
import org.restlet.ext.wadl.ParameterInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.ext.wadl.RequestInfo;
import org.restlet.ext.wadl.ResponseInfo;

import com.aasenov.restapi.resources.FileResource;
import com.aasenov.restapi.resources.FileType;

/**
 * This class adds additional description to methods, provided by {@link FileResource} class.
 */
public class FileResourceDoc extends FileResource {

    public FileResourceDoc() {
        super();
        setName("File resource");
        setDescription("Provide operation over single file.");
    }

    @Override
    protected void describeGet(MethodInfo info) {
        super.describeGet(info);

        DocumentationInfo doc = new DocumentationInfo("Download requested file.");
        doc.setTitle("Description");
        info.getDocumentations().add(doc);

        // describe parameters
        List<ParameterInfo> parameters = new ArrayList<ParameterInfo>();
        parameters.add(new ParameterInfo("Authorization", true, "xsi:string", ParameterStyle.HEADER,
                "HTTP Basic Authentication."));
        ParameterInfo paramToAdd = new ParameterInfo(PARAM_TYPE, false, "xsi:string", ParameterStyle.QUERY,
                "Type of file to download.");
        paramToAdd.setDefaultValue(FileType.SPEECH.toString());
        paramToAdd.getOptions().add(new OptionInfo(FileType.SPEECH.toString(), null));
        paramToAdd.getOptions().add(new OptionInfo(FileType.ORIGINAL.toString(), null));
        parameters.add(paramToAdd);
        if (info.getRequest() == null) {
            info.setRequest(new RequestInfo());
        }
        info.getRequest().setParameters(parameters);

        // responses
        info.getResponse().setDocumentation("If operation was successful.");
        for (RepresentationInfo repr : info.getResponse().getRepresentations()) {
            if (repr.getMediaType() == MediaType.APPLICATION_ALL) {
                repr.setDocumentation("Original file, that was stored to the system.");
            } else if (repr.getMediaType() == MediaType.AUDIO_WAV) {
                repr.setDocumentation("Generated speech file.");
            }
        }
        ResponseInfo errorResponse = new ResponseInfo("In case of error during authentication.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_UNAUTHORIZED);
        info.getResponses().add(errorResponse);
        errorResponse = new ResponseInfo("If some mandatory parameter or attribute is missing.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_BAD_REQUEST);
        info.getResponses().add(errorResponse);
        errorResponse = new ResponseInfo("In file with given hash doesn't exists.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_NOT_FOUND);
        info.getResponses().add(errorResponse);
    }

    @Override
    protected void describeDelete(MethodInfo info) {
        super.describeDelete(info);

        DocumentationInfo doc = new DocumentationInfo("Delete requested file.");
        doc.setTitle("Description");
        info.getDocumentations().add(doc);

        // describe parameters
        List<ParameterInfo> parameters = new ArrayList<ParameterInfo>();
        parameters.add(new ParameterInfo("Authorization", true, "xsi:string", ParameterStyle.HEADER,
                "HTTP Basic Authentication."));

        if (info.getRequest() == null) {
            info.setRequest(new RequestInfo());
        }
        info.getRequest().setParameters(parameters);

        // responses
        info.getResponse().setDocumentation("If file deletion was successful.");
        ResponseInfo errorResponse = new ResponseInfo("In case of error during authentication.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_UNAUTHORIZED);
        info.getResponses().add(errorResponse);
        errorResponse = new ResponseInfo("If some mandatory parameter or attribute is missing.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_BAD_REQUEST);
        info.getResponses().add(errorResponse);
        errorResponse = new ResponseInfo("In file with given hash doesn't exists.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_NOT_FOUND);
        info.getResponses().add(errorResponse);
        errorResponse = new ResponseInfo("In case of error during file deletion.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_FAILED_DEPENDENCY);
        info.getResponses().add(errorResponse);
    }

    @Override
    protected void describePost(MethodInfo info) {
        super.describePost(info);

        DocumentationInfo doc = new DocumentationInfo("Share file with specified users.");
        doc.setTitle("Description");
        info.getDocumentations().add(doc);

        // describe parameters
        List<ParameterInfo> parameters = new ArrayList<ParameterInfo>();
        parameters.add(new ParameterInfo("Authorization", true, "xsi:string", ParameterStyle.HEADER,
                "HTTP Basic Authentication."));
        parameters.add(new ParameterInfo(PARAM_SHARE_USERS, true, "xsi:string", ParameterStyle.QUERY,
                "List of user mails, to share file with, separated with commas."));
        if (info.getRequest() == null) {
            info.setRequest(new RequestInfo());
        }
        info.getRequest().setParameters(parameters);

        // responses
        info.getResponse().setDocumentation("If file sharing was successful.");
        ResponseInfo errorResponse = new ResponseInfo("In case of error during authentication.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_UNAUTHORIZED);
        info.getResponses().add(errorResponse);
        errorResponse = new ResponseInfo("If some mandatory parameter or attribute is missing.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_BAD_REQUEST);
        info.getResponses().add(errorResponse);
        errorResponse = new ResponseInfo("In file with given hash doesn't exists.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_NOT_FOUND);
        info.getResponses().add(errorResponse);
        errorResponse = new ResponseInfo("In case of error during file sharing.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_FAILED_DEPENDENCY);
        info.getResponses().add(errorResponse);
    }

}
