package com.aasenov.database.objects;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Base class that represent record in the database.
 * 
 */
@XmlRootElement(name = "DatabaseItem")
@XmlType(name = "DatabaseItem")
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
     * Use only for serialization.
     */
    @Deprecated
    protected DatabaseItem() {
    }

    /**
     * Construct databse item.
     * 
     * @param id - id of the object in database.
     */
    public DatabaseItem(String id) {
        mID = id;
    }

    /**
     * Getter for the {@link DatabaseItem#mID} field.
     * 
     * @return the {@link DatabaseItem#mID} value.
     */
    @XmlAttribute(name = "id")
    public String getID() {
        return mID;
    }

    /**
     * Setter for the {@link DatabaseItem#mID} field. Do not use, as this is the key of this object!!!
     * 
     * @param id the {@link DatabaseItem#mID} to set
     */
    @Deprecated
    public void setID(String id) {
        mID = id;
    }

    /**
     * Getter for the {@link DatabaseItem#mRowID} field.
     * 
     * @return the {@link DatabaseItem#mRowID} value.
     */
    @XmlAttribute(name = "rowid")
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
    @XmlAttribute(name = "forUpdate")
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
