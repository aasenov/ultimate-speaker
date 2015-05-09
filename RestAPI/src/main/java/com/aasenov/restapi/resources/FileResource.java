package com.aasenov.restapi.resources;

import org.apache.log4j.Logger;
import org.restlet.resource.ServerResource;

import com.aasenov.database.objects.DatabaseTable;
import com.aasenov.database.objects.FileItem;

public class FileResource extends ServerResource {

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(FileResource.class);

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static DatabaseTable<FileItem> mFilesTable;

}
