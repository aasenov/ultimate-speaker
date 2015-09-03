package com.aasenov.restapi.resources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import com.aasenov.database.objects.FileItem;
import com.aasenov.restapi.managers.FileManager;
import com.aasenov.restapi.objects.FileItemWeb;
import com.aasenov.restapi.objects.FileItemsList;
import com.aasenov.restapi.util.Helper;
import com.fasterxml.jackson.core.JsonProcessingException;

public class FilesResource extends WadlServerResource {

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(FilesResource.class);

    /**
     * Default number of results to return during listing files from database.
     */
    protected static final int DEFAULT_PAGE_SIZE = 100;

    /**
     * Parameter containing start point for listing.
     */
    protected static final String PARAM_START = "start";

    /**
     * Parameter containing number of files to list.
     */
    protected static final String PARAM_COUNT = "count";

    /**
     * Parameter containing type of response to return.
     */
    protected static final String PARAM_OUT = "out";

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
    @Get("json|xml")
    public Representation list() {
        String startString = this.getQuery().getFirstValue(PARAM_START);
        int start = 0;
        if (startString != null && !startString.isEmpty()) {
            try {
                start = Integer.parseInt(startString);
            } catch (NumberFormatException ex) {
                sLog.error(ex.getMessage(), ex);
            }
        }

        String countString = this.getQuery().getFirstValue(PARAM_COUNT);
        int count = DEFAULT_PAGE_SIZE;
        if (countString != null && !countString.isEmpty()) {
            try {
                count = Integer.parseInt(countString);
            } catch (NumberFormatException ex) {
                sLog.error(ex.getMessage(), ex);
            }
        }

        String typeOfResponse = this.getQuery().getFirstValue(PARAM_OUT);
        if (typeOfResponse == null || typeOfResponse.isEmpty()) {
            typeOfResponse = ResponseType.JSON.toString();
        }

        String userID = getRequest().getChallengeResponse().getIdentifier();
        List<String> fileIDsForUser = FileManager.getInstance().getFilesForUser(userID, start, count);
        List<FileItem> originalFiles = FileManager.getInstance().getFiles(fileIDsForUser);
        long totalCount = FileManager.getInstance().getTotalFilesForUser(userID);
        List<FileItemWeb> filesToSend = retrieveFileItemsForWeb(originalFiles);
        FileItemsList result = new FileItemsList(totalCount, filesToSend);
        try {
            if (typeOfResponse.equalsIgnoreCase(ResponseType.XML.toString())) {
                return new StringRepresentation(Helper.formatXMLOutputResult(result), MediaType.APPLICATION_XML);
            } else {
                return new StringRepresentation(Helper.formatJSONOutputResult(result), MediaType.APPLICATION_JSON);
            }
        } catch (JsonProcessingException e) {
            sLog.error(e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return new StringRepresentation("\"Error formatting resulting objects during listing.\"",
                    MediaType.TEXT_PLAIN);
        }
    }

    /**
     * Construct list of files ready for representation.
     * 
     * @param originalFiles - original files retrieved from database.
     * @return Constructed list, containing all needed information for files.
     */
    private List<FileItemWeb> retrieveFileItemsForWeb(List<FileItem> originalFiles) {
        List<FileItemWeb> result = new ArrayList<FileItemWeb>();
        for (FileItem file : originalFiles) {
            double rating = FileManager.getInstance().getRatingForFile(file.getID());
            result.add(new FileItemWeb(file, rating));
        }
        return result;
    }

    /**
     * Method that handle file uploads.
     * 
     * @param entity - to retrieve files from
     * @return Result from file upload operation.
     */
    @Post("multipart:json")
    public Representation addFile(Representation entity) {
        Representation rep = null;
        if (entity != null) {
            if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
                // 1/ Create a factory for disk-based file items
                DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setSizeThreshold(FileManager.STREAM_READ_SIZE);

                // 2/ Create a new file upload handler
                RestletFileUpload upload = new RestletFileUpload(factory);
                upload.setHeaderEncoding("UTF-8"); // set header encoding to UTF-8 to support cirilyc chars
                List<org.apache.commons.fileupload.FileItem> items;
                try {
                    // 3/ Request is parsed by the handler which generates a list of FileItems
                    items = upload.parseRequest(getRequest());

                    List<String> fileIDs = new ArrayList<String>();
                    String userID = getRequest().getChallengeResponse().getIdentifier();
                    Iterator<org.apache.commons.fileupload.FileItem> it = items.iterator();
                    while (it.hasNext()) {
                        String fileID = FileManager.getInstance().handleFileUpload(it.next(), userID);
                        if (fileID != null) {
                            fileIDs.add(fileID);
                        }
                    }

                    if (fileIDs.isEmpty()) {
                        setStatus(Status.CLIENT_ERROR_CONFLICT);
                        rep = new StringRepresentation("File already exists!", MediaType.TEXT_PLAIN);
                    } else {
                        rep = new StringRepresentation(Helper.formatJSONOutputResult(fileIDs),
                                MediaType.APPLICATION_JSON);
                    }
                } catch (Exception e) {
                    // The message of all thrown exception is sent back to
                    // client as simple plain text
                    setStatus(Status.SERVER_ERROR_INTERNAL);
                    sLog.error(e.getMessage(), e);
                    rep = new StringRepresentation(e.getMessage(), MediaType.TEXT_PLAIN);
                }
            } else {
                // other format != multipart form data
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                rep = new StringRepresentation("Multipart/form-data required", MediaType.TEXT_PLAIN);
            }
        } else {
            // POST request with no entity.
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            rep = new StringRepresentation("Error - entity is null.", MediaType.TEXT_PLAIN);
        }

        return rep;
    }
}
