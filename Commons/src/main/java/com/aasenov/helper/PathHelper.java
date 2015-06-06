package com.aasenov.helper;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Helper class that provide common methods related to file paths.
 */
public class PathHelper {

    /**
     * Keep jar containing folder in field to avoid computing it every time.
     */
    private static String sJarContainingFolder = null;

    /**
     * Retrieve folder containing running jar file.
     * 
     * @return Absolute path to folder.
     */
    public static synchronized String getJarContainingFolder() {
        if (sJarContainingFolder == null) {
            try {
                sJarContainingFolder = URLDecoder.decode(ClassLoader.getSystemClassLoader().getResource(".").getPath(),
                        "UTF-8");
            } catch (UnsupportedEncodingException e) {
                sJarContainingFolder = "." + File.separator;
            }
        }
        return sJarContainingFolder;
    }
}
