package com.aasenov.restapi.managers;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.aasenov.searchengine.provider.SearchManagerProvider;

/**
 * Use this thread to asynchronously index local files.
 */
public class IndexThread extends Thread {
    /**
     * Logger instance of this class.
     */
    private static Logger sLog = Logger.getLogger(IndexThread.class);

    /**
     * Size of buffer used for reading.
     */
    private static int READ_BUFF_SIZE = 65536; // 64KB buffer

    /**
     * Path to document for indexing.
     */
    String mDocumentToIndex;

    /**
     * ID of indexed document.
     */
    String mDocumentID;

    /**
     * Title of indexed document.
     */
    String mDocumentTitle;

    /**
     * Title of indexed document.
     */
    String mUserID;

    /**
     * Id of original file.
     */
    String mFileID;

    /**
     * Construct thread for indexing.
     * 
     * @param documentToIndex - Path to document for indexing.
     * @param documentID - ID of indexed document.
     * @param documentTitle - Title of indexed document.
     * @param userID - ID of owner of the document.
     * @param fileID - ID of original file.
     */
    public IndexThread(String documentToIndex, String documentID, String documentTitle, String userID, String fileID) {
        mDocumentToIndex = documentToIndex;
        mDocumentID = documentID;
        mDocumentTitle = documentTitle;
        mUserID = userID;
        mFileID = fileID;
    }

    @Override
    public void run() {
        // read document content
        FileInputStream inStream = null;
        byte[] buff = new byte[READ_BUFF_SIZE];
        ByteArrayOutputStream result = null;
        try {
            // detect language as it's not extracted dynamically from the input file and move to original stream
            inStream = new FileInputStream(mDocumentToIndex);
            result = new ByteArrayOutputStream(READ_BUFF_SIZE);
            int len = inStream.read(buff);
            if (len > 0) {
                result.write(buff, 0, len);
            }

            SearchManagerProvider.getDefaultSearchManager().indexDocument(new String(result.toByteArray()),
                    mDocumentID, mDocumentTitle, mUserID, mFileID);
            if (sLog.isDebugEnabled()) {
                sLog.debug(String.format("Successfully index %s file.", mDocumentTitle));
            }
        } catch (Exception e) {
            sLog.error(e.getMessage(), e);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                }
            }
            if (result != null) {
                try {
                    result.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
