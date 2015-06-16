package com.aasenov.restapi.resources;

import java.io.File;

import org.apache.log4j.Logger;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.aasenov.database.objects.DatabaseTable;
import com.aasenov.database.objects.FileItem;
import com.aasenov.restapi.managers.FileManager;

public class FileResource extends ServerResource {

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
     * Download file with given hash taken from URL.<br/>
     * Options for downloading:<br/>
     * <ul>
     * <li><b>speech</b> - whether to download speech file or not. Default to true.</li>
     * <li><b>original</b> - whether to download original file or not</li>
     * </ul>
     * 
     * @return Original file or generated speech.
     */
    @Get
    public Representation download() {
        Representation rep;

        String fileHash = (String) this.getRequestAttributes().get("hash");
        if (fileHash == null || fileHash.isEmpty()) {
            rep = new StringRepresentation("No file hash specified", MediaType.TEXT_PLAIN);
            setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "No file hash specified");
            return rep;
        }

        String downloadSpeechString = this.getQuery().getFirstValue("speech");
        boolean downloadSpeech = false;
        if (downloadSpeechString != null && !downloadSpeechString.isEmpty()) {
            try {
                downloadSpeech = Boolean.parseBoolean(downloadSpeechString);
            } catch (Exception ex) {
                sLog.error(ex.getMessage(), ex);
            }
        }

        String downloadOriginalString = this.getQuery().getFirstValue("original");
        boolean downloadOriginal = false;
        if (downloadOriginalString != null && !downloadOriginalString.isEmpty()) {
            try {
                downloadOriginal = Boolean.parseBoolean(downloadOriginalString);
            } catch (Exception ex) {
                sLog.error(ex.getMessage(), ex);
            }
        }

        FileItem result = mFilesTable.get(fileHash);
        if (result != null) {
            File fileToDownload = null;
            if (downloadOriginal && !downloadSpeech) {
                fileToDownload = new File(result.getLocation());
                rep = new FileRepresentation(fileToDownload, MediaType.APPLICATION_ALL);
            } else {
                fileToDownload = new File(result.getSpeechLocation());
                rep = new FileRepresentation(fileToDownload, MediaType.AUDIO_WAV);
            }

            Disposition disp = new Disposition(Disposition.TYPE_ATTACHMENT);
            disp.setFilename(fileToDownload.getName());
            disp.setSize(fileToDownload.length());
            rep.setDisposition(disp);
            return rep;
        }

        String message = String.format("File with hash '%s' doesn't exists.", fileHash);
        sLog.info(message);
        setStatus(Status.CLIENT_ERROR_NOT_FOUND, message);
        return new StringRepresentation(message, MediaType.TEXT_PLAIN);
    }

    /**
     * Alter file with given hash taken from URL.<br/>
     * Options for altering:<br/>
     * <ul>
     * <li><b>delete</b> - whether to delete the file.</li>
     * </ul>
     * 
     * @return Result from altering.
     */
    @Post
    public Representation alterFile(Representation entity) throws ResourceException {
        Representation rep;

        String fileHash = (String) this.getRequestAttributes().get("hash");
        if (fileHash == null || fileHash.isEmpty()) {
            rep = new StringRepresentation("No file hash specified", MediaType.TEXT_PLAIN);
            setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "No file hash specified");
            return rep;
        }

        final Form form = new Form(entity);
        String deleteFileString = form.getFirstValue("delete");
        boolean deleteFile = false;
        if (deleteFileString != null && !deleteFileString.isEmpty()) {
            try {
                deleteFile = Boolean.parseBoolean(deleteFileString);
            } catch (Exception ex) {
                sLog.error(ex.getMessage(), ex);
            }
        }

        FileItem result = mFilesTable.get(fileHash);
        if (result != null) {
            if (deleteFile) {
                if (FileManager.getInstance().handleFileDeletion(result)) {
                    return new StringRepresentation(String.format("Successfully deleting file '%s'", result.getName()),
                            MediaType.TEXT_PLAIN);
                } else {
                    String message = String.format("Problem during file '%s' deletion. Please check the logs!",
                            result.getName());
                    sLog.info(message);
                    setStatus(Status.CLIENT_ERROR_FAILED_DEPENDENCY, message);
                    return new StringRepresentation(message, MediaType.TEXT_PLAIN);
                }
            } else {
                String message = "Unkown operation selected.";
                sLog.info(message);
                setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, message);
                return new StringRepresentation(message, MediaType.TEXT_PLAIN);
            }
        }

        String message = String.format("File with hash '%s' doesn't exists.", fileHash);
        sLog.info(message);
        setStatus(Status.CLIENT_ERROR_NOT_FOUND, message);
        return new StringRepresentation(message, MediaType.TEXT_PLAIN);
    }

}
