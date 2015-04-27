package com.aasenov.restapi.resources;

import java.util.List;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.aasenov.database.objects.DatabaseTable;
import com.aasenov.database.objects.FileItem;
import com.aasenov.restapi.util.Helper;
import com.fasterxml.jackson.core.JsonProcessingException;

public class FilesResource extends ServerResource {

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(FilesResource.class);

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static DatabaseTable<FileItem> mFilesTable;

    @Override
    public void init(Context arg0, Request arg1, Response arg2) {
        super.init(arg0, arg1, arg2);
        mFilesTable = new DatabaseTable<FileItem>("Files", new FileItem());
        // mFilesTable.addAll(new FileItem("file1", "hash1", "location1", "speechLocation1"), new FileItem("file1",
        // "hash2", "location1", "speechLocation1"),
        // new FileItem("file1", "hash3", "location1", "speechLocation1"));
    }

    @Get
    public Representation list() {
        String startString = this.getQuery().getFirstValue("start");
        int start = 0;
        if (startString != null && !startString.isEmpty()) {
            try{
                start = Integer.parseInt(startString);
            } catch (NumberFormatException ex){
                sLog.error(ex.getMessage(),ex);
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
            typeOfResponse = "json";
        }

        List<FileItem> result = mFilesTable.getPage(start, count);
        try {
            if (typeOfResponse.equalsIgnoreCase("xml")) {
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
}
