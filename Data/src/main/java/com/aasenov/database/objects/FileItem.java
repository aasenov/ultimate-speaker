package com.aasenov.database.objects;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class FileItem extends DatabaseItem {
    /**
     * Default serial versioni UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(FileItem.class);

    private String mName;
    private String mHash;
    private String mLocation;
    private String mSpeechLocation;

    public FileItem() {
        super();
    }

    public FileItem(String name, String hash, String location, String speechLocation) {
        this();
        mName = name;
        mHash = hash;
        mLocation = location;
        mSpeechLocation = speechLocation;
    }

    /**
     * Getter for the {@link FileItem#mName} field.
     *
     * @return the {@link FileItem#mName} value.
     */
    public String getName() {
        return mName;
    }

    /**
     * Setter for the {@link FileItem#mName} field.
     *
     * @param name the {@link FileItem#mName} to set
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Getter for the {@link FileItem#mHash} field.
     *
     * @return the {@link FileItem#mHash} value.
     */
    public String getHash() {
        return mHash;
    }

    /**
     * Setter for the {@link FileItem#mHash} field.
     *
     * @param hash the {@link FileItem#mHash} to set
     */
    public void setHash(String hash) {
        mHash = hash;
    }

    /**
     * Getter for the {@link FileItem#mLocation} field.
     *
     * @return the {@link FileItem#mLocation} value.
     */
    public String getLocation() {
        return mLocation;
    }

    /**
     * Setter for the {@link FileItem#mLocation} field.
     *
     * @param location the {@link FileItem#mLocation} to set
     */
    public void setLocation(String location) {
        mLocation = location;
    }

    /**
     * Getter for the {@link FileItem#mSpeechLocation} field.
     *
     * @return the {@link FileItem#mSpeechLocation} value.
     */
    public String getSpeechLocation() {
        return mSpeechLocation;
    }

    /**
     * Setter for the {@link FileItem#mSpeechLocation} field.
     *
     * @param speechLocation the {@link FileItem#mSpeechLocation} to set
     */
    public void setSpeechLocation(String speechLocation) {
        mSpeechLocation = speechLocation;
    }

    public static String getDatabaseTableProperties() {
        return "(rowid INT PRIMARY KEY NOT NULL, name TEXT NOT NULL, hash TEXT UNUQUE NOT NULL, location TEXT NOT NULL, speechLocation TEXT, payload BLOB)";
    }

    @Override
    public String getInsertStatement() {
        return "VALUES (?,?,?,?,?,?)";
    }

    @Override
    public void fillInsertStatementValues(PreparedStatement insertStatement) {
        try {// skip rowID, it will be generated automatically.
            insertStatement.setString(2, getName());
            insertStatement.setString(3, getHash());
            insertStatement.setString(4, getLocation());
            insertStatement.setString(5, getSpeechLocation());
            insertStatement.setBytes(6, serializeObject());
        } catch (SQLException e) {
            sLog.error(e.getMessage(), e);
        }
    }

    @Override
    public String getUpdateStatement() {
        return "name='?',hash='?',location='?',speechLocation='?', payload='?' WHERE rowid='?'";
    }

    @Override
    public void fillUpdatetStatementValues(PreparedStatement insertStatement) {
        try {
            insertStatement.setString(2, getName());
            insertStatement.setString(3, getHash());
            insertStatement.setString(4, getLocation());
            insertStatement.setString(5, getSpeechLocation());
            insertStatement.setBytes(6, serializeObject());
            insertStatement.setInt(7, getRowID());
        } catch (SQLException e) {
            sLog.error(e.getMessage(), e);
        }
    }

}
