package com.aasenov.restapi.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.aasenov.database.objects.DatabaseTable;
import com.aasenov.database.objects.FileItem;
import com.aasenov.helper.ConfigHelper;
import com.aasenov.helper.ConfigProperty;
import com.aasenov.parser.ContentMetadata;
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
     * @return ID of file that was uploaded, <b>Null</b> if such doesn't exists.
     * @throws Exception in case of error.
     */
    public String handleFileUpload(org.apache.commons.fileupload.FileItem fileItem) throws Exception {
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
            boolean fileExists = false;
            synchronized (mFilesTable) {
                FileItem existingFile = mFilesTable.get(hash);
                if (existingFile == null) {
                    mFilesTable.add(new FileItem(name, hash, file.getCanonicalPath(), null));
                } else {
                    fileExists = true;
                    // do not allow duplicate files
                    sLog.error(String.format(
                            "File with hash '%s' already exists. Existing file name is '%s'. Skipping file upload!",
                            hash, existingFile.getName()));
                    result = null;
                }
            }

            if (fileExists) {
                // delete downloaded file
                file.delete();
            } else {
                // file doesn't exists - process it further
                // parse the file content
                File parsedFile = new File(mParsedFilesDir, file.getName());
                ContentMetadata metadata = extractFileContent(file.getCanonicalPath(), parsedFile.getCanonicalPath());

                // based on language detected - generate speech
                File speechFile = new File(mSpeechFilesDir, file.getName());
                TextSynthesizerProvider.getDefaultSynthesizer(
                        SynthesizerLanguage.valueOf(metadata.getLanguage().toString())).synthesizeFromFileToFile(
                        parsedFile.getCanonicalPath(), speechFile.getCanonicalPath());

                // update db record
                synchronized (mFilesTable) {
                    FileItem exitingFile = mFilesTable.get(hash);
                    if (exitingFile == null) {
                        sLog.error(String.format("File with hash %s doesn't exists. Unable to update speech location",
                                hash));
                    } else {
                        exitingFile.setSpeechLocation(speechFile.getCanonicalPath());
                        // copy locations from previous file and delete stored file
                        mFilesTable.add(exitingFile);
                    }
                }

                // index created file - asynchronously
                new IndexThread(parsedFile.getCanonicalPath(), hash, name).start();
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
     * @return <b>True</b> if deletion was successful, <b>False</b> otherwise.
     */
    public boolean handleFileDeletion(FileItem file) {
        try {
            // delete from database
            synchronized (mFilesTable) {
                mFilesTable.remove(file.getID());
            }

            // delete from file system
            for (String fileToDeleteStr : new String[] { file.getLocation(), file.getSpeechLocation() }) {
                File fileToDelete = new File(fileToDeleteStr);
                if (fileToDelete.exists()) {
                    sLog.info("Deleting file: " + fileToDelete.getAbsolutePath());
                    fileToDelete.delete();
                }
            }

            // delete search index
            SearchManagerProvider.getDefaultSearchManager().deleteIndexedDocument(file.getID());
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
            return false;
        }
        return true;
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
