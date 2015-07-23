package com.aasenov.restapi.resources;

import java.io.File;

import org.apache.log4j.Logger;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import com.aasenov.database.objects.DatabaseTable;
import com.aasenov.database.objects.FileItem;
import com.aasenov.restapi.managers.FileManager;

public class FileResource extends WadlServerResource {

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(FileResource.class);

    /**
     * Database table containing file items.
     */
    private static DatabaseTable<FileItem> mFilesTable = new DatabaseTable<FileItem>(FileItem.DEFAULT_TABLE_NAME,
            new FileItem(null));

    /**
     * Attribute containing hash of file to operate to.
     */
    protected static final String ATTR_HASH = "hash";

    /**
     * Parameter containing type of file to download.
     */
    protected static final String PARAM_TYPE = "type";

    /**
     * Download file with given hash taken from URL.<br/>
     * Options for downloading:<br/>
     * <ul>
     * <li><b>speech</b> - whether to download speech file or not. Default to true.</li>
     * <li><b>original</b> - whether to download original file or not</li>
     * </ul>
     * 
     * @return Original file or generated speech.
     */
    @Get("appAll|wav")
    public Representation download() {
        Representation rep;

        String fileHash = (String) this.getRequestAttributes().get(ATTR_HASH);
        if (fileHash == null || fileHash.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("No file hash specified", MediaType.TEXT_PLAIN);
        }

        String typeOfFileToDownload = this.getQuery().getFirstValue(PARAM_TYPE);
        if (typeOfFileToDownload == null || typeOfFileToDownload.isEmpty()) {
            typeOfFileToDownload = FileType.SPEECH.toString();
        }
        FileItem result = mFilesTable.get(fileHash);
        if (result != null) {
            File fileToDownload = null;
            Disposition disp = new Disposition(Disposition.TYPE_ATTACHMENT);
            if (typeOfFileToDownload.equalsIgnoreCase(FileType.ORIGINAL.toString())) {
                fileToDownload = new File(result.getLocation());
                disp.setFilename(result.getName());
                rep = new FileRepresentation(fileToDownload, MediaType.APPLICATION_ALL);
            } else {
                fileToDownload = new File(result.getSpeechLocation());
                disp.setFilename(changeExtension(result.getName(), "wav"));
                rep = new FileRepresentation(fileToDownload, MediaType.AUDIO_WAV);
            }

            disp.setSize(fileToDownload.length());
            rep.setDisposition(disp);
            return rep;
        }

        String message = String.format("File with hash '%s' doesn't exists.", fileHash);
        sLog.info(message);
        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        return new StringRepresentation(message, MediaType.TEXT_PLAIN);
    }

    /**
     * Delete file with given hash taken from URL.<br/>
     * 
     * @return Result from deleting.
     */
    @Delete("txt")
    public Representation deleteFile(Representation entity) throws ResourceException {
        String fileHash = (String) this.getRequestAttributes().get("hash");
        if (fileHash == null || fileHash.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("No file hash specified", MediaType.TEXT_PLAIN);
        }

        String userID = getRequest().getChallengeResponse().getIdentifier();
        FileItem result = mFilesTable.get(fileHash);
        if (result != null) {
            if (FileManager.getInstance().handleFileDeletion(result, userID)) {
                return new StringRepresentation(String.format("Successfully deleting file '%s'", result.getName()),
                        MediaType.TEXT_PLAIN);
            } else {
                String message = String.format("Problem during file '%s' deletion. Please check the logs!",
                        result.getName());
                sLog.info(message);
                setStatus(Status.CLIENT_ERROR_FAILED_DEPENDENCY);
                return new StringRepresentation(message, MediaType.TEXT_PLAIN);
            }
        }

        String message = String.format("File with hash '%s' doesn't exists.", fileHash);
        sLog.info(message);
        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        return new StringRepresentation(message, MediaType.TEXT_PLAIN);
    }

    /**
     * Change extension of given file.
     * 
     * @param fileName - name of file with old extension.
     * @param newExtension - new extension to set.
     * @return Changed file name.
     */
    private String changeExtension(String fileName, String newExtension) {
        if (fileName.indexOf('.') > 0) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        return String.format("%s.%s", fileName, newExtension);
    }

}
