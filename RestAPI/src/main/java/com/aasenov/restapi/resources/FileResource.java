package com.aasenov.restapi.resources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.management.relation.RelationNotFoundException;

import org.apache.log4j.Logger;
import org.restlet.data.CharacterSet;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

import com.aasenov.database.err.NotInRangeException;
import com.aasenov.database.objects.FileItem;
import com.aasenov.restapi.managers.FileManager;
import com.aasenov.restapi.managers.UserManager;
import com.aasenov.restapi.mapper.MapperException;
import com.aasenov.restapi.mapper.MapperProvider;
import com.aasenov.restapi.objects.FileSlidesList;

public class FileResource extends WadlServerResource {

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(FileResource.class);

    /**
     * Attribute containing hash of file to operate to.
     */
    protected static final String ATTR_HASH = "hash";

    /**
     * Parameter containing type of file to download.
     */
    protected static final String PARAM_TYPE = "type";

    /**
     * Parameter containing mails of users to share file with.
     */
    protected static final String PARAM_SHARE_USERS = "shareWith";

    /**
     * Parameter containing rating to be applied to file.
     */
    protected static final String PARAM_RATING = "rating";

    /**
     * Download file with given hash taken from URL.<br/>
     * Options for downloading:<br/>
     * <ul>
     * <li><b>speech</b> - whether to download speech file or not. Default to true.</li>
     * <li><b>slides</b> - whether to get information for slides of presentation file.</li>
     * <li><b>original</b> - whether to download original file or not</li>
     * </ul>
     * 
     * @return Original file or generated speech.
     */
    @Get("appAll|wav|json|xml")
    public Representation download() {
        sLog.info("Request for file downloading received!");
        Representation rep;

        String fileHash = (String) this.getRequestAttributes().get(ATTR_HASH);
        if (fileHash == null || fileHash.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            String message = "No file hash specified";
            sLog.error(message);
            return new StringRepresentation(message, MediaType.TEXT_PLAIN);
        }

        String typeOfFileToDownload = this.getQuery().getFirstValue(PARAM_TYPE);
        if (typeOfFileToDownload == null || typeOfFileToDownload.isEmpty()) {
            typeOfFileToDownload = FileType.SPEECH.toString();
        }
        FileItem result = FileManager.getInstance().getFile(fileHash);
        if (result != null) {
            if (typeOfFileToDownload.equalsIgnoreCase(FileType.SLIDES.toString())) {
                sLog.info(String.format("Retrieving slides for file '%s'", result.getName()));
                // we should return slides information
                List<String> images = new ArrayList<String>();
                List<String> speeches = new ArrayList<String>();
                Scanner in = null;
                try {
                    in = new Scanner(new File(result.getSpeechBySlidesLocation()));
                    in.useDelimiter(FileManager.BASE64_SEPARATOR);
                    while (in.hasNext()) {
                        String base64String = in.next();
                        if (base64String.startsWith(FileManager.SLIDE_IMG_START)) {
                            images.add(base64String.substring(FileManager.SLIDE_IMG_START.length()));
                        } else if (base64String.startsWith(FileManager.SLIDE_SPEECHIMG_START)) {
                            speeches.add(base64String.substring(FileManager.SLIDE_SPEECHIMG_START.length()));
                        } else {
                            sLog.error(String.format("Unable to detect information contained by string read: %s",
                                    base64String));
                        }
                    }
                } catch (IOException e) {
                    sLog.error(e.getMessage(), e);
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
                try {
                    rep = MapperProvider.getMapper(getRequest())
                            .getRepresentation(new FileSlidesList(images, speeches));
                    rep.setCharacterSet(CharacterSet.UTF_8);// add support for Cyrilic chars
                    sLog.info(String.format("Slides retrieving for '%s' successful.", result.getName()));
                } catch (MapperException e) {
                    sLog.error(e.getMessage(), e);
                    setStatus(Status.SERVER_ERROR_INTERNAL);
                    return new StringRepresentation("Error formatting resulting objects during viewing.",
                            MediaType.TEXT_PLAIN);
                }
            } else {
                sLog.info(String.format("Downloading file '%s'.", result.getName()));

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
            }
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
        sLog.info("Request for file deleting received!");

        String fileHash = (String) this.getRequestAttributes().get("hash");
        if (fileHash == null || fileHash.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            String message = "No file hash specified";
            sLog.error(message);
            return new StringRepresentation(message, MediaType.TEXT_PLAIN);
        }

        String userID = getRequest().getChallengeResponse().getIdentifier();
        FileItem result = FileManager.getInstance().getFile(fileHash);
        if (result != null) {
            sLog.info(String.format("Deleting file '%s'", result.getName()));
            if (FileManager.getInstance().handleFileDeletion(result, userID)) {
                String message = String.format("File '%s' successfully deleted.", result.getName());
                sLog.info(message);
                return new StringRepresentation(message, MediaType.TEXT_PLAIN);
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
     * Share file with given hash with other user.
     * 
     * @param entity - post request body.
     */
    @Post("form:txt")
    public Representation shareFile(Representation entity) throws ResourceException {
        sLog.info("Request for file sharing received!");

        final Form form = new Form(entity);
        String shareWith = form.getFirstValue(PARAM_SHARE_USERS);
        String fileHash = (String) this.getRequestAttributes().get("hash");

        if (fileHash == null || fileHash.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            String message = "No file hash specified";
            sLog.error(message);
            return new StringRepresentation(message, MediaType.TEXT_PLAIN);
        }
        if (shareWith == null || shareWith.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            String message = "No users to share with specified";
            sLog.error(message);
            return new StringRepresentation(message, MediaType.TEXT_PLAIN);
        }

        // verify correct values are given
        String[] userIDsArray = shareWith.split(",");
        for (String userID : userIDsArray) {
            if (!UserManager.getInstance().checkUserExists(userID)) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                String message = String.format("User with id '%s' doesn't exists.", userID);
                sLog.error(message);
                return new StringRepresentation(message, MediaType.TEXT_PLAIN);
            }
        }

        List<String> userIDs = Arrays.asList(userIDsArray);
        FileItem result = FileManager.getInstance().getFile(fileHash);
        if (result != null) {
            if (FileManager.getInstance().shareFile(result, userIDs)) {
                String message = String.format("Successfully sharing file '%s' with users '%s'", result.getName(),
                        Arrays.toString(shareWith.split(",")));
                sLog.info(message);
                return new StringRepresentation(message, MediaType.TEXT_PLAIN);
            } else {
                String message = String.format("Problem during file '%s' sharing. Please check the logs!",
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

    @Put("form:txt")
    public Representation rate(Representation entity) throws ResourceException {
        sLog.info("Request for file rating received!");

        final Form form = new Form(entity);
        String rating = form.getFirstValue(PARAM_RATING);
        String fileHash = (String) this.getRequestAttributes().get("hash");

        // validate
        if (fileHash == null || fileHash.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            String message = "No file hash specified";
            sLog.error(message);
            return new StringRepresentation(message, MediaType.TEXT_PLAIN);
        }
        if (rating == null || rating.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            String message = "No rating for file specified";
            sLog.error(message);
            return new StringRepresentation(message, MediaType.TEXT_PLAIN);
        }
        double doubleRating;
        try {
            doubleRating = Double.parseDouble(rating);
        } catch (NumberFormatException ex) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            String message = String.format("Rating is not a number '%s'", rating);
            sLog.error(message);
            return new StringRepresentation(message, MediaType.TEXT_PLAIN);
        }

        // update rating
        String userID = getRequest().getChallengeResponse().getIdentifier();
        try {
            double newRating = FileManager.getInstance().updateRatingForFile(fileHash, userID, doubleRating);
            sLog.info(String.format("Rating for file '%s' and user '%s' successfully updated to '%s'.", fileHash,
                    userID, doubleRating));
            return new StringRepresentation(Double.toString(newRating), MediaType.TEXT_PLAIN);
        } catch (RelationNotFoundException e) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            String message = String.format("Given user-file relation '%s'-'%s' doesn't exists! is not a number '%s'",
                    userID, fileHash, rating) + e.getMessage();
            sLog.error(message, e);
            return new StringRepresentation(message, MediaType.TEXT_PLAIN);
        } catch (NotInRangeException e) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            String message = String.format("Given rating '%s' is not in range!", doubleRating) + e.getMessage();
            sLog.error(message, e);
            return new StringRepresentation(message, MediaType.TEXT_PLAIN);
        }
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
