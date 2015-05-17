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

import org.apache.log4j.Logger;

import com.aasenov.database.objects.DatabaseTable;
import com.aasenov.database.objects.FileItem;
import com.aasenov.parser.ContentMetadata;
import com.aasenov.parser.provider.ParserProvider;
import com.aasenov.restapi.resources.FilesResource;

/**
 * Managers used to handle operations with files
 */
public class FileManager {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(FilesResource.class);

    /**
     * Size to read during file uploading.
     */
    public static final int STREAM_READ_SIZE = 1000240;

    private static final String sOriginalFilesDir = "origFiles"; // TODO make configurable
    private static final String sParsedFilesDir = "parsedFiles"; // TODO make configurable
    private static final String sSpeechFilesDir = "speechFiles"; // TODO make configurable

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

    private FileManager() {
        mFilesTable = new DatabaseTable<FileItem>("Files", new FileItem());
        // initialize directories
        String[] dirsToInit = new String[] { sOriginalFilesDir, sParsedFilesDir, sSpeechFilesDir };
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
     * @return Name of file that was uploaded, <b>Null</b> if such doesn't exists.
     * @throws Exception in case of error.
     */
    public String handleFileUpload(org.apache.commons.fileupload.FileItem fileItem) throws Exception {
        String name = fileItem.getName();
        if (name == null) {
            sLog.error(String.format("Unable to determine file name of %s='%s'. Skipping.", fileItem.getFieldName(),
                    new String(fileItem.get(), "UTF-8")));
        } else {
            // store in FS
            File file = new File(sOriginalFilesDir, name);
            if (file.exists()) {
                // generate random string for duplicate files. If their hashes match this file will be
                // deleted.
                file = new File(sOriginalFilesDir, name + UUID.randomUUID());
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
                FileItem exitingFile = mFilesTable.get(hash);
                if (exitingFile == null) {
                    mFilesTable.add(new FileItem(name, hash, file.getCanonicalPath(), null));
                } else {
                    fileExists = true;
                    // copy locations from previous file and delete stored file
                    mFilesTable
                            .add(new FileItem(name, hash, exitingFile.getLocation(), exitingFile.getSpeechLocation()));
                }
            }

            if (fileExists) {
                // delete downloaded file
                file.delete();
            } else {
                // file doesn't exists - process it further
                // parse the file content
                ContentMetadata metadata = extractFileContent(file.getCanonicalPath(),
                        new File(sParsedFilesDir, file.getName()).getCanonicalPath());
                System.out.println(metadata);
            }
        }

        return name;
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
}
