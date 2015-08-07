package com.aasenov.restapi.managers;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.restlet.engine.util.Base64;

import com.aasenov.database.objects.DatabaseTable;
import com.aasenov.database.objects.FileItem;
import com.aasenov.database.objects.UserFileRelationDatabaseTable;
import com.aasenov.database.objects.UserFileRelationItem;
import com.aasenov.helper.ConfigHelper;
import com.aasenov.helper.ConfigProperty;
import com.aasenov.parser.ContentMetadata;
import com.aasenov.parser.PPTParseResult;
import com.aasenov.parser.PPTParser;
import com.aasenov.parser.provider.ParserProvider;
import com.aasenov.searchengine.provider.SearchManagerProvider;
import com.aasenov.synthesis.provider.SynthesizerLanguage;
import com.aasenov.synthesis.provider.TextSynthesizerProvider;

/**
 * Managers used to handle operations with files
 */
public class FileManager {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(FileManager.class);

    /**
     * Size to read during file uploading.
     */
    public static final int STREAM_READ_SIZE = 1000240;

    /**
     * Base64 string separator. Use comma, as it's not a valid Base64 string.
     */
    public static final String BASE64_SEPARATOR = ",";

    /**
     * Start string to denote base64 encoded image.
     */
    public static final String SLIDE_IMG_START = "img:";

    /**
     * Start string to denote base64 encoded speech.
     */
    public static final String SLIDE_SPEECHIMG_START = "speech:";

    /**
     * Path under which all files will be stored.
     */
    private final String mStoragePath = ConfigHelper.getInstance().getConfigPropertyValue(ConfigProperty.StorageDir);

    /**
     * Path under which original files will be stored.
     */
    private final String mOriginalFilesDir = new File(mStoragePath, "origFiles").getAbsolutePath();

    /**
     * Path under which parsed files will be stored.
     */
    private final String mParsedFilesDir = new File(mStoragePath, "parsedFiles").getAbsolutePath();

    /**
     * Path under which speech files will be stored.
     */
    private final String mSpeechFilesDir = new File(mStoragePath, "speechFiles").getAbsolutePath();

    /**
     * Database table containing file items.
     */
    private DatabaseTable<FileItem> mFilesTable;

    /**
     * Database table containing user-file relations.
     */
    private UserFileRelationDatabaseTable mUserFileRelTable;

    /**
     * Static instance of this class.
     */
    private static FileManager sInstance;

    /**
     * Retrieve static instance of this manager.
     * 
     * @return Initialized {@link FileManager} object.
     */
    public static synchronized FileManager getInstance() {
        if (sInstance == null) {
            sInstance = new FileManager();
        }
        return sInstance;
    }

    /**
     * Destroy statically initialize instance of this class.
     */
    public static synchronized void destroy() {
        sInstance = null;
    }

    private FileManager() {
        mFilesTable = new DatabaseTable<FileItem>(FileItem.DEFAULT_TABLE_NAME, new FileItem(null));
        mUserFileRelTable = new UserFileRelationDatabaseTable(UserFileRelationItem.DEFAULT_TABLE_NAME);
        // initialize directories
        String[] dirsToInit = new String[] { mOriginalFilesDir, mParsedFilesDir, mSpeechFilesDir };
        for (String dir : dirsToInit) {
            File dirToInit = new File(dir);
            if (!dirToInit.exists() && !dirToInit.mkdirs()) {
                throw new RuntimeException("Unable to create directory: " + dir);
            }
        }
    }

    /**
     * Method that handle file upload.
     * 
     * @param fileItem - item to be uploaded.
     * @param userID - ID of user that this file belongs to.
     * 
     * @return ID of file that was uploaded, <b>Null</b> if such doesn't exists.
     * @throws Exception in case of error.
     */
    public String handleFileUpload(org.apache.commons.fileupload.FileItem fileItem, String userID) throws Exception {
        String result = null;
        String name = fileItem.getName();
        if (name == null) {
            sLog.error(String.format("Unable to determine file name of %s='%s'. Skipping.", fileItem.getFieldName(),
                    new String(fileItem.get(), "UTF-8")));
        } else {
            // store in FS
            File file = new File(mOriginalFilesDir, name);
            if (file.exists()) {
                // generate random string for duplicate files. If their hashes match this file will be
                // deleted.
                file = new File(mOriginalFilesDir, name + UUID.randomUUID());
            }

            // compute md5 checksum during file upload to prevent reading file twice.
            String hash = "tempHash";
            InputStream fis = null;
            OutputStream out = null;
            byte[] buffer = new byte[STREAM_READ_SIZE];
            try {
                fis = fileItem.getInputStream();
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
                result = hash;
            } finally {
                if (fis != null) {
                    fis.close();
                }
                if (out != null) {
                    out.close();
                }
            }

            // store in database
            UserFileRelationItem userFileRel = new UserFileRelationItem(userID, hash);
            FileItem existingFile = mFilesTable.get(hash);
            if (existingFile == null) {
                // ranam in order to store files by hash
                File newFile = new File(mOriginalFilesDir, hash);
                if (file.renameTo(newFile)) {
                    file = newFile;
                    FileItem newDBFile = new FileItem(name, hash, file.getCanonicalPath(), null, null, null);
                    try {
                        mFilesTable.add(newDBFile);
                        mUserFileRelTable.add(userFileRel);
                    } catch (Exception ex) {
                        // Probably another thread already crate file with same hash
                        sLog.error(ex.getMessage(), ex);
                        existingFile = mFilesTable.get(hash);
                    }
                } else {
                    sLog.error(String.format("Unable to rename file %s to %s. Skip uploading.",
                            file.getCanonicalFile(), newFile.getCanonicalPath()));
                    file.delete();
                    return null;
                }
            }

            if (existingFile != null) {
                // check whether this file exist for current user
                UserFileRelationItem existingReletaion = mUserFileRelTable.get(userFileRel.getID());
                if (existingReletaion == null) {
                    // this user has no access to this file, add one
                    try {
                        mUserFileRelTable.add(userFileRel);
                    } catch (Exception ex) {
                        sLog.error(ex.getMessage(), ex);
                        result = null;
                    }
                } else {
                    // do not allow duplicate files
                    sLog.error(String.format(
                            "File with hash '%s' already exists. Existing file name is '%s'. Skipping file upload!",
                            hash, existingFile.getName()));
                    result = null;
                }
            }

            if (existingFile != null) {
                // delete downloaded file
                file.delete();

                if (result != null) {
                    // new relation is created. Index file for new user
                    new IndexThread(existingFile.getParsedLocation(), userFileRel.getID(), name, userID, hash).start();
                }
            } else {
                // file doesn't exists - process it further
                // parse the file content
                File parsedFile = new File(mParsedFilesDir, hash);
                File speechBySlidesLocation = null;
                ContentMetadata metadata = extractFileContent(file.getCanonicalPath(), parsedFile.getCanonicalPath());
                if (PPTParser.PRESENTATION_CONTENT_TYPES.contains(metadata.getContentType())) {
                    // this is ppt file - process as such
                    speechBySlidesLocation = new File(mOriginalFilesDir, hash + ".slides");
                    InputStream is = null;
                    BufferedWriter slidesToSpeechOut = null;
                    try {
                        is = new FileInputStream(file.getCanonicalPath());
                        PPTParseResult pptParseResult = ParserProvider.getDefaultPPTParser().parse(is,
                                metadata.getContentType());
                        slidesToSpeechOut = new BufferedWriter(new FileWriter(speechBySlidesLocation));

                        if (pptParseResult.getSlidesImagesBase64Encoded().size() != pptParseResult.getSlidesText()
                                .size()) {
                            sLog.error(String.format("Num slide texts %s and images %s differ!", pptParseResult
                                    .getSlidesText().size(), pptParseResult.getSlidesImagesBase64Encoded().size()));
                        } else {
                            // generate speech for slide texts
                            File tmpSpeechFile = new File(mSpeechFilesDir, hash);
                            for (int i = 0; i < pptParseResult.getSlidesText().size(); i++) {
                                TextSynthesizerProvider.getDefaultSynthesizer(
                                        SynthesizerLanguage.valueOf(metadata.getLanguage().toString()))
                                        .synthesizeToFile(pptParseResult.getSlidesText().get(i),
                                                tmpSpeechFile.getCanonicalPath());
                                // transfer bytes
                                ByteArrayOutputStream tmpOut = null;
                                FileInputStream tmpIn = null;
                                try {
                                    tmpOut = new ByteArrayOutputStream();
                                    tmpIn = new FileInputStream(tmpSpeechFile);
                                    IOUtils.copy(tmpIn, tmpOut);
                                } catch (IOException e) {
                                    sLog.error(e.getMessage(), e);
                                } finally {
                                    if (tmpIn != null) {
                                        try {
                                            tmpIn.close();
                                        } catch (IOException e) {
                                        }
                                    }
                                    if (tmpOut != null) {
                                        try {
                                            tmpOut.close();
                                        } catch (IOException e) {
                                        }
                                    }
                                }

                                if (i != 0) {
                                    slidesToSpeechOut.write(BASE64_SEPARATOR);
                                }
                                slidesToSpeechOut.write(SLIDE_SPEECHIMG_START);
                                slidesToSpeechOut.write(Base64.encode(tmpOut.toByteArray(), false));
                                slidesToSpeechOut.write(BASE64_SEPARATOR);
                                slidesToSpeechOut.write(SLIDE_IMG_START);
                                slidesToSpeechOut.write(pptParseResult.getSlidesImagesBase64Encoded().get(i));
                            }
                            if (!tmpSpeechFile.delete()) {
                                sLog.error(String.format("Unable to delete temporary hash file '%s'!",
                                        tmpSpeechFile.getCanonicalPath()));
                            }
                        }
                    } catch (Exception e) {
                        sLog.error(e.getMessage(), e);
                        result = null;
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e) {
                            }
                        }
                        if (slidesToSpeechOut != null) {
                            try {
                                slidesToSpeechOut.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                }
                // based on language detected - generate speech
                File speechFile = new File(mSpeechFilesDir, hash);
                TextSynthesizerProvider.getDefaultSynthesizer(
                        SynthesizerLanguage.valueOf(metadata.getLanguage().toString())).synthesizeFromFileToFile(
                        parsedFile.getCanonicalPath(), speechFile.getCanonicalPath());

                // update db record
                FileItem exitingFile = mFilesTable.get(hash);
                if (exitingFile == null) {
                    sLog.error(String
                            .format("File with hash %s doesn't exists. Unable to update speech location", hash));
                } else {
                    exitingFile.setSpeechLocation(speechFile.getCanonicalPath());
                    if (speechBySlidesLocation != null) {
                        exitingFile.setSpeechBySlidesLocation(speechBySlidesLocation.getCanonicalPath());
                    }
                    exitingFile.setParsedLocation(parsedFile.getCanonicalPath());
                    mFilesTable.add(exitingFile);
                }

                // index created file - asynchronously
                new IndexThread(parsedFile.getCanonicalPath(), userFileRel.getID(), name, userID, hash).start();
            }
        }

        return result;
    }

    /**
     * Extract text content from file with given path.
     * 
     * @param filePath - path of file to extract from.
     * @param resultPath - where to place extracted text.
     * @return Metadata collected during parsing.
     */
    private ContentMetadata extractFileContent(String filePath, String resultPath) {
        ContentMetadata result = null;
        InputStream is = null;
        OutputStream out = null;
        try {
            is = new FileInputStream(filePath);
            out = new FileOutputStream(resultPath);
            result = new ContentMetadata();
            ParserProvider.getDefaultParser().parse(is, result, out);
        } catch (Exception e) {
            sLog.error(e.getMessage(), e);
            result = null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        return result;
    }

    /**
     * Delete given file from the system.
     * 
     * @param file - file to delete.
     * @param userID - id of user deleting file.
     * @return <b>True</b> if deletion was successful, <b>False</b> otherwise.
     */
    public boolean handleFileDeletion(FileItem file, String userID) {
        try {
            UserFileRelationItem userFileRel = new UserFileRelationItem(userID, file.getID());
            mUserFileRelTable.remove(userFileRel.getID());

            // check whether other users has access to the file
            long numUsers = mUserFileRelTable.getTotalUsersForFile(file.getID());
            if (numUsers == 0) {
                // delete from database
                mFilesTable.remove(file.getID());

                // delete from file system
                for (String fileToDeleteStr : new String[] { file.getLocation(), file.getSpeechLocation(),
                        file.getParsedLocation() }) {
                    File fileToDelete = new File(fileToDeleteStr);
                    if (fileToDelete.exists()) {
                        sLog.info("Deleting file: " + fileToDelete.getAbsolutePath());
                        fileToDelete.delete();
                    }
                }
            }

            // delete search index
            SearchManagerProvider.getDefaultSearchManager().deleteIndexedDocument(userFileRel.getID());
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
            return false;
        }
        return true;
    }

    /**
     * Share given file with specified users.
     * 
     * @param file - file to share.
     * @param userIDs - IDs of users to share with.
     * @return <b>True</b> if operation was successful, <b>False</b> otherwise.
     */
    public boolean shareFile(FileItem file, List<String> userIDs) {
        try {
            for (String userID : userIDs) {
                UserFileRelationItem userFileRel = new UserFileRelationItem(userID, file.getID());
                boolean relationCreated = false;
                if (mUserFileRelTable.get(userFileRel.getID()) == null) {
                    try {
                        mUserFileRelTable.add(userFileRel);
                        relationCreated = true;
                    } catch (Exception ex) {
                        sLog.error(ex.getMessage(), ex);
                    }
                }

                if (relationCreated) {
                    // index created file - asynchronously
                    new IndexThread(file.getParsedLocation(), userFileRel.getID(), file.getName(), userID,
                            file.getHash()).start();
                } else {
                    sLog.info(String.format("Relation between file '%s' and user '%s' alredy exists.", file.getName(),
                            userID));
                }
            }
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
            return false;
        }
        return true;
    }

    /**
     * Retrieve file with given ID.
     * 
     * @param fileID - ID of file to retrieve.
     * @return File with given ID, or <b>Null</b> if none.
     */
    public FileItem getFile(String fileID) {
        if (fileID != null && !fileID.isEmpty()) {
            return mFilesTable.get(fileID);
        }
        return null;
    }

    /**
     * Retrieve all files with given IDs.
     * 
     * @param fileIDs - IDs of files to retrieve.
     * @return List of objects with given IDs.
     */
    public List<FileItem> getFiles(List<String> fileIDs) {
        if (fileIDs != null && !fileIDs.isEmpty()) {
            return mFilesTable.getAll(fileIDs);
        }
        return new ArrayList<FileItem>();
    }

    /**
     * Retrieve files for given user.
     * 
     * @param userID - ID of user to retrieve files for.
     * @param startIndex - index from where to start selecting.
     * @param count - number of fileIDs to return.
     * @return List of fileIDs, that given user has access to.
     */
    public List<String> getFilesForUser(String userID, int startIndex, int count) {
        if (userID != null && !userID.isEmpty()) {
            return mUserFileRelTable.getFilesForUser(userID, startIndex, count);
        }
        return new ArrayList<String>();
    }

    /**
     * Retrieve number of files for given user.
     * 
     * @param userID - ID of user to retrieve files for.
     * @return Number of files that given user has.
     */
    public long getTotalFilesForUser(String userID) {
        if (userID != null && !userID.isEmpty()) {
            return mUserFileRelTable.getTotalFilesForUser(userID);
        }
        return 0;
    }

    /**
     * Cleanup storage folders.
     */
    public void deleteStorage() {
        for (String folderToDelete : new String[] { mOriginalFilesDir, mParsedFilesDir, mSpeechFilesDir }) {
            sLog.info("Deleting dir: " + folderToDelete);
            try {
                FileUtils.deleteDirectory(new File(folderToDelete));
            } catch (IOException e) {
                sLog.error(String.format("Error deleting '%s' directory.", folderToDelete), e);
            }
        }
    }
}
