package com.aasenov.database.objects;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
     * Shows whether given item is for update or not.
     */
    private boolean mForUpdate = false;

    /**
     * Row primary key.
     */
    private long mRowID;

    /**
     * ID of current item, different from row ID.
     */
    private String mID;

    /**
     * Getter for the {@link DatabaseItem#mID} field.
     * 
     * @return the {@link DatabaseItem#mID} value.
     */
    public String getID() {
        return mID;
    }

    /**
     * Setter for the {@link DatabaseItem#mID} field.
     * 
     * @param id the {@link DatabaseItem#mID} to set
     */
    public void setID(String id) {
        mID = id;
    }

    /**
     * Getter for the {@link DatabaseItem#mRowID} field.
     * 
     * @return the {@link DatabaseItem#mRowID} value.
     */
    public long getRowID() {
        return mRowID;
    }

    /**
     * Setter for the {@link DatabaseItem#mRowID} field.
     * 
     * @param rowID the {@link DatabaseItem#mRowID} to set
     */
    public void setRowID(long rowID) {
        mRowID = rowID;
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

    @Override
    public String toString() {
        return String.format("rowID=%s id=%s forUpdate=%s ", getRowID(), getID(), isForUpdate());
    }

    /**
     * Retrieve properties used to create database table for current object.
     * 
     * @return Table properties used to store current object fields.
     */
    public abstract String getDatabaseTableProperties();

    /**
     * Retrieve columns used for indexing.
     * 
     * @return Columns for indexing.
     */
    public abstract String getIndexColumns();

    /**
     * Get statement for inserting row in database.
     * 
     * @return Constructed statement.
     */
    public abstract String getInsertStatement();

    /**
     * Fill statement for inserting row in database.
     */
    public abstract void fillInsertStatementValues(PreparedStatement insertStatement) throws SQLException;

    /**
     * Get statement for updating row in database.
     * 
     * @return Constructed statement.
     */
    public abstract String getUpdateStatement();

    /**
     * Fill statement for updating row in database.
     */
    public abstract void fillUpdatetStatementValues(PreparedStatement insertStatement) throws SQLException;

}
