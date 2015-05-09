package com.aasenov.restapi.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

import com.aasenov.database.manager.DatabaseProvider;
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

    /**
     * Size to read during file uploading.
     */
    private static final int STREAM_READ_SIZE = 1000240;

    private static DatabaseTable<FileItem> mFilesTable;

    static {
        // clean DB on start
        DatabaseProvider.getDefaultManager().deleteAllTables();
        mFilesTable = new DatabaseTable<FileItem>("Files", new FileItem());
    }

    /**
     * Enable options method to allow file upload through Ajax.
     */
    @Options
    public void optionsMethod() {
        enableCORS();
    }

    /**
     * List all files from database.
     * 
     * @return
     */
    @Get
    public Representation list() {
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

    @Post
    public Representation addFile(Representation entity) {
        enableCORS();
        Representation rep = null;
        if (entity != null) {
            if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
                // 1/ Create a factory for disk-based file items
                DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setSizeThreshold(STREAM_READ_SIZE);

                // 2/ Create a new file upload handler
                RestletFileUpload upload = new RestletFileUpload(factory);
                List<org.apache.commons.fileupload.FileItem> items;
                try {
                    // 3/ Request is parsed by the handler which generates a list of FileItems
                    items = upload.parseRequest(getRequest());

                    Map<String, String> props = new HashMap<String, String>();
                    List<String> fileNames = new ArrayList<String>();
                    File file = null;
                    Iterator<org.apache.commons.fileupload.FileItem> it = items.iterator();
                    while (it.hasNext()) {
                        org.apache.commons.fileupload.FileItem fi = it.next();
                        String name = fi.getName();
                        if (name == null) {
                            props.put(fi.getFieldName(), new String(fi.get(), "UTF-8"));
                        } else {
                            // store in FS
                            file = new File(name);
                            if (file.exists()) {
                                // generate random string for duplicate files. If their hashes match this file will be
                                // deleted.
                                file = new File(name + UUID.randomUUID());
                            }
                            fileNames.add(name);

                            // compute md5 checksum during file upload to prevent reading file twice.
                            String hash = "tempHash";
                            InputStream fis = null;
                            OutputStream out = null;
                            byte[] buffer = new byte[STREAM_READ_SIZE];
                            try {
                                fis = fi.getInputStream();
                                out = new FileOutputStream(file);

                                MessageDigest md = MessageDigest.getInstance("MD5");
                                int numRead;
                                do {
                                    numRead = fis.read(buffer);
                                    if (numRead > 0) {
                                        // write file to FS
                                        out.write(buffer, 0, numRead);
                                        // compute hash
                                        md.update(buffer, 0, numRead);
                                    }
                                } while (numRead != -1);

                                hash = new BigInteger(1, md.digest()).toString(16);
                            } finally {
                                if (fis != null) {
                                    fis.close();
                                }
                                if (out != null) {
                                    out.close();
                                }
                            }

                            // store in database
                            FileItem exitingFile = mFilesTable.get(hash);
                            if (exitingFile == null) {
                                mFilesTable.add(new FileItem(name, hash, file.getCanonicalPath(), null));
                            } else {
                                // copy locations from previous file and delete stored file
                                mFilesTable.add(new FileItem(name, hash, exitingFile.getLocation(), exitingFile
                                        .getSpeechLocation()));
                                file.delete();
                            }
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
