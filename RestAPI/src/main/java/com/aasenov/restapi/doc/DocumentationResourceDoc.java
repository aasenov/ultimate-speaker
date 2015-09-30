package com.aasenov.restapi.doc;

import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;

import com.aasenov.restapi.resources.DocumentationResource;
import com.aasenov.restapi.resources.UsersResource;

/**
 * This class adds additional description to methods, provided by {@link UsersResource} class.
 */
public class DocumentationResourceDoc extends DocumentationResource {

    public DocumentationResourceDoc() {
        super();
        setName("Documentation generation resource");
        setDescription("Use to retrieve various information about RestAPI resources.");
    }

    @Override
    protected void describeGet(MethodInfo info) {
        super.describeGet(info);

        DocumentationInfo doc = new DocumentationInfo("Generate information about all RestAPI resources.");
        doc.setTitle("Description");
        info.getDocumentations().add(doc);
    }
}
