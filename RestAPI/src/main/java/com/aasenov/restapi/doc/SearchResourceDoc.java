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

import com.aasenov.restapi.resources.SearchResource;
import com.aasenov.restapi.resources.SearchType;

/**
 * This class adds additional description to methods, provided by {@link SearchResource} class.
 */
public class SearchResourceDoc extends SearchResource {

    public SearchResourceDoc() {
        super();
        setName("Search resource");
        setDescription("Provide operation for files searching.");
    }

    @Override
    protected void describePost(MethodInfo info) {
        super.describePost(info);

        DocumentationInfo doc = new DocumentationInfo("Perform search over user file.");
        doc.setTitle("Description");
        info.getDocumentations().add(doc);

        // describe parameters
        List<ParameterInfo> parameters = new ArrayList<ParameterInfo>();
        parameters.add(new ParameterInfo("Authorization", true, "xsi:string", ParameterStyle.HEADER,
                "HTTP Basic Authentication."));
        ParameterInfo paramToAdd = new ParameterInfo(PARAM_ACTION, true, "xsi:string", ParameterStyle.QUERY,
                "Type of search to perform.");
        paramToAdd.getOptions().add(new OptionInfo(SearchType.SEARCH.toString(), null));
        paramToAdd.getOptions().add(new OptionInfo(SearchType.SUGGEST.toString(), null));
        parameters.add(paramToAdd);
        parameters.add(new ParameterInfo(PARAM_QUERY, true, "xsi:string", ParameterStyle.QUERY,
                "Search query to execute. You can use quotes to perform phrase search."));
        paramToAdd = new ParameterInfo(PARAM_START, false, "xsi:integer", ParameterStyle.QUERY,
                "Start point from where file searching should begin.");
        paramToAdd.setDefaultValue(Integer.toString(DEFAULT_START_FROM));
        parameters.add(paramToAdd);
        paramToAdd = new ParameterInfo(PARAM_COUNT, false, "xsi:integer", ParameterStyle.QUERY,
                "Number of search hits to return.");
        paramToAdd.setDefaultValue(Integer.toString(DEFAULT_COUNT));
        parameters.add(paramToAdd);
        if (info.getRequest() == null) {
            info.setRequest(new RequestInfo());
        }
        info.getRequest().setParameters(parameters);

        // responses
        info.getResponse().setDocumentation("If operation was successful.");
        for (RepresentationInfo repr : info.getResponse().getRepresentations()) {
            if (repr.getMediaType() == MediaType.TEXT_HTML) {
                repr.setDocumentation("HTMl formatted list of search hits.");
            }
        }
        ResponseInfo errorResponse = new ResponseInfo("In case of error during authentication.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_UNAUTHORIZED);
        info.getResponses().add(errorResponse);
        errorResponse = new ResponseInfo("If some mandatory parameter or attribute is missing.");
        errorResponse.getStatuses().add(Status.CLIENT_ERROR_BAD_REQUEST);
        info.getResponses().add(errorResponse);
        errorResponse = new ResponseInfo("In case of error during request processing.");
        errorResponse.getStatuses().add(Status.SERVER_ERROR_INTERNAL);
        info.getResponses().add(errorResponse);
    }
}
