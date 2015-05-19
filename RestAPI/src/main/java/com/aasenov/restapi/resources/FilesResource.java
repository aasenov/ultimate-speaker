package com.aasenov.restapi.resources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.log4j.Logger;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;

import com.aasenov.database.objects.DatabaseTable;
import com.aasenov.database.objects.FileItem;
import com.aasenov.restapi.managers.FileManager;
import com.aasenov.restapi.util.Helper;
import com.fasterxml.jackson.core.JsonProcessingException;

public class FilesResource extends ServerResource {

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(FilesResource.class);

    /**
     * Default number of results to return during listing files from database.
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * Database table containing file items.
     */
    private static DatabaseTable<FileItem> mFilesTable = new DatabaseTable<FileItem>("Files", new FileItem());

    /**
     * Enable options method to allow file upload through Ajax.
     */
    @Options
    public void optionsMethod() {
        enableCORS();
    }

    /**
     * List all files from database.<br/>
     * Options for listing:<br/>
     * <ul>
     * <li><b>start</b> - start point for returned page of files</li>
     * <li><b>count</b> - number of files to return. Default is DEFAULT_PAGE_SIZE</li>
     * <li><b>out</b> - type of return result. One of {@link ResponseType} constants.</li>
     * </ul>
     * 
     * @return List of files, formatted based on passed criteria parameters.
     */
    @Get
    public Representation list() {
        enableCORS();
        String startString = this.getQuery().getFirstValue("start");
        int start = 0;
        if (startString != null && !startString.isEmpty()) {
            try {
                start = Integer.parseInt(startString);
            } catch (NumberFormatException ex) {
                sLog.error(ex.getMessage(), ex);
            }
        }

        String countString = this.getQuery().getFirstValue("count");
        int count = DEFAULT_PAGE_SIZE;
        if (countString != null && !countString.isEmpty()) {
            try {
                count = Integer.parseInt(countString);
            } catch (NumberFormatException ex) {
                sLog.error(ex.getMessage(), ex);
            }
        }

        String typeOfResponse = this.getQuery().getFirstValue("out");
        if (typeOfResponse == null || typeOfResponse.isEmpty()) {
            typeOfResponse = ResponseType.JSON.toString();
        }

        List<FileItem> result = mFilesTable.getPage(start, count);
        try {
            if (typeOfResponse.equalsIgnoreCase(ResponseType.XML.toString())) {
                return new StringRepresentation(Helper.formatXMLOutputResult(result), MediaType.APPLICATION_XML);
            } else {
                return new StringRepresentation(Helper.formatJSONOutputResult(result), MediaType.APPLICATION_JSON);
            }
        } catch (JsonProcessingException e) {
            sLog.error(e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return new StringRepresentation("Error formatting resulting objects", MediaType.APPLICATION_JSON);
        }
    }

    /**
     * Method that handle file uploads.
     * 
     * @param entity - to retrieve files from
     * @return Result from file upload operation.
     */
    @Post
    public Representation addFile(Representation entity) {
        enableCORS();
        Representation rep = null;
        if (entity != null) {
            if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
                // 1/ Create a factory for disk-based file items
                DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setSizeThreshold(FileManager.STREAM_READ_SIZE);

                // 2/ Create a new file upload handler
                RestletFileUpload upload = new RestletFileUpload(factory);
                List<org.apache.commons.fileupload.FileItem> items;
                try {
                    // 3/ Request is parsed by the handler which generates a list of FileItems
                    items = upload.parseRequest(getRequest());

                    List<String> fileNames = new ArrayList<String>();
                    Iterator<org.apache.commons.fileupload.FileItem> it = items.iterator();
                    while (it.hasNext()) {
                        String fileName = FileManager.getInstance().handleFileUpload(it.next());
                        if (fileName != null) {
                            fileNames.add(fileName);
                        }
                    }

                    rep = new StringRepresentation(Helper.formatJSONOutputResult(fileNames), MediaType.APPLICATION_JSON);
                } catch (Exception e) {
                    // The message of all thrown exception is sent back to
                    // client as simple plain text
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    sLog.error(e.getMessage(), e);
                    rep = new StringRepresentation(e.getMessage(), MediaType.TEXT_PLAIN);
                }
            } else {
                // other format != multipart form data
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                rep = new StringRepresentation("Multipart/form-data required", MediaType.TEXT_PLAIN);
            }
        } else {
            // POST request with no entity.
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            rep = new StringRepresentation("Error", MediaType.TEXT_PLAIN);
        }

        return rep;
    }

    /**
     * Enable Cross domain origin in order to allow uploads from multiple UI sources.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void enableCORS() {
        Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
        if (responseHeaders == null) {
            responseHeaders = new Series(Header.class);
            getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
        }
        responseHeaders.add(new Header("Access-Control-Allow-Origin", "*"));
    }
}
