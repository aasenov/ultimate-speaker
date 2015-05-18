package com.aasenov.searchengine;

import java.io.File;

import org.apache.log4j.Logger;

/**
 * Utility class, used by searchengine.
 */
public class Utils {

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(Utils.class);

    /**
     * Empty and delete a folder (and subfolders).
     * 
     * @param folder folder to empty
     */
    public static void rmdir(final File folder) {
        // check if folder file is a real folder
        if (folder.isDirectory()) {
            File[] list = folder.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    File tmpF = list[i];
                    if (tmpF.isDirectory()) {
                        rmdir(tmpF);
                    }
                    tmpF.delete();
                }
            }
            if (!folder.delete()) {
                sLog.error("can't delete folder : " + folder);
            }
        }
    }

    /**
     * Constructing error response.
     * 
     * @param errorMessage - message to include in response.
     * @return JSON formatted response.
     */
    public static String constructErrorResponse(String errorMessage) {
        return String.format("{\"error\" : \"true\", \"errorMessage\" : \"%s\"}", errorMessage);
    }
}
