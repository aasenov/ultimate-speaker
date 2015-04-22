package com.aasenov.database;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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

    /**
     * Deserialize object from given byte array.
     * 
     * @return Result from deserialization.
     */
    public static Object deserializeObject(byte[] arr) {
        Object result = null;
        ByteArrayInputStream byteIn = null;
        ObjectInputStream in = null;
        try {
            byteIn = new ByteArrayInputStream(arr);
            in = new ObjectInputStream(byteIn);
            result = in.readObject();
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
        } finally {
            if (byteIn != null) {
                try {
                    byteIn.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        return result;
    }

    /**
     * Escape given string value to not break database rules.
     * 
     * @param value - String to excape.
     * @return Escaped string.
     */
    public static String escapeString(String value) {
        String s = value.replace("'", "''");
        return s;
    }
}
