package com.aasenov.database;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;

public class DatabaseUtil {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(DatabaseUtil.class);

    /**
     * Serialize given object to byte array.
     * 
     * @return Result from serialization.
     */
    public static byte[] serializeObject(Object obj) {
        byte[] result = null;
        ByteArrayOutputStream byteOut = null;
        ObjectOutputStream out = null;
        try {
            byteOut = new ByteArrayOutputStream();
            out = new ObjectOutputStream(byteOut);
            out.writeObject(obj);
            result = byteOut.toByteArray();
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
        } finally {
            if (byteOut != null) {
                try {
                    byteOut.close();
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
