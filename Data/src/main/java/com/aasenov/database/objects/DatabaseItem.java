package com.aasenov.database.objects;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;

import org.apache.log4j.Logger;

/**
 * Base class that represent record in the database.
 *
 */
public abstract class DatabaseItem implements Serializable {

    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(DatabaseItem.class);

    /**
     * Shows whether given item is for update or not.
     */
    private boolean mForUpdate = false;

    /**
     * Row primary key.
     */
    private int mRowID;

    /**
     * Getter for the {@link DatabaseItem#mRowID} field.
     *
     * @return the {@link DatabaseItem#mRowID} value.
     */
    public int getRowID() {
        return mRowID;
    }

    /**
     * Setter for the {@link DatabaseItem#mRowID} field.
     *
     * @param rowID the {@link DatabaseItem#mRowID} to set
     */
    public void setRowID(int rowID) {
        this.mRowID = rowID;
    }

    /**
     * Getter for the {@link DatabaseItem#mForUpdate} field.
     *
     * @return the {@link DatabaseItem#mForUpdate} value.
     */
    public boolean isForUpdate() {
        return mForUpdate;
    }

    /**
     * Setter for the {@link DatabaseItem#mForUpdate} field.
     *
     * @param forUpdate the {@link DatabaseItem#mForUpdate} to set
     */
    public void setForUpdate(boolean forUpdate) {
        mForUpdate = forUpdate;
    }

    /**
     * Serialize given object to byte array.
     * 
     * @return Result from serialization.
     */
    protected byte[] serializeObject() {
        byte[] result = null;
        ByteArrayOutputStream byteOut = null;
        ObjectOutputStream out = null;
        try {
            byteOut = new ByteArrayOutputStream();
            out = new ObjectOutputStream(byteOut);
            out.writeObject(this);
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
     * Get statement for inserting row in database.
     * 
     * @return Constructed statement.
     */
    public abstract String getInsertStatement();

    /**
     * Fill statement for inserting row in database.
     */
    public abstract void fillInsertStatementValues(PreparedStatement insertStatement);

    /**
     * Get statement for updating row in database.
     * 
     * @return Constructed statement.
     */
    public abstract String getUpdateStatement();

    /**
     * Fill statement for updating row in database.
     */
    public abstract void fillUpdatetStatementValues(PreparedStatement insertStatement);

}
