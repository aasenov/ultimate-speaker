package com.aasenov.database.objects;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aasenov.database.DatabaseUtil;
import com.aasenov.database.manager.DatabaseProvider;

@XmlRootElement(name = "FileItem")
@XmlType(name = "FileItem")
public class FileItem extends DatabaseItem {
    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default name of table, containing these items.
     */
    public static String DEFAULT_TABLE_NAME = "Files";

    private String mName;
    private String mHash;
    private String mLocation;
    private String mSpeechLocation;

    /**
     * Do not use, as this is the key of this object!!!
     */
    @Deprecated
    public FileItem() {
        super();
    }

    public FileItem(String hash) {
        super(hash);
        mHash = hash;
    }

    public FileItem(String name, String hash, String location, String speechLocation) {
        this(hash);
        mName = name;
        mLocation = location;
        mSpeechLocation = speechLocation;
    }

    /**
     * Getter for the {@link FileItem#mName} field.
     * 
     * @return the {@link FileItem#mName} value.
     */
    @XmlElement(name = "Name")
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
    @XmlElement(name = "Hash")
    public String getHash() {
        return mHash;
    }

    /**
     * Setter for the {@link FileItem#mHash} field. Do not use. Used only for
     * 
     * @param hash the {@link FileItem#mHash} to set
     */
    @Deprecated
    public void setHash(String hash) {
        mHash = hash;
        setID(hash);
    }

    /**
     * Getter for the {@link FileItem#mLocation} field.
     * 
     * @return the {@link FileItem#mLocation} value.
     */
    @XmlElement(name = "Location")
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
    @XmlElement(name = "SpeechLocation")
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

    @XmlTransient
    @Override
    public String getDatabaseTableProperties() {
        switch (DatabaseProvider.getDatabaseType()) {
        case SQLite:
        default:
            return "(rowid INTEGER PRIMARY KEY NOT NULL, ID TEXT UNIQUE NOT NULL, Name TEXT NOT NULL,Hash TEXT UNIQUE NOT NULL, Location TEXT NOT NULL, SpeechLocation TEXT, Payload BLOB)";
        }
    }

    @XmlTransient
    @Override
    public String getIndexColumns() {
        switch (DatabaseProvider.getDatabaseType()) {
        case SQLite:
        default:
            return "ID, Hash";
        }
    }

    @Override
    public String toString() {
        return String.format("%s name=%s hash=%s location=%s speechLocation=%s", super.toString(), getName(),
                getHash(), getLocation(), getSpeechLocation());
    }

    @XmlTransient
    @Override
    public String getInsertStatement() {
        // skip rowID, it will be generated automatically.
        return "(ID, Name, Hash, Location, SpeechLocation, Payload) VALUES (?,?,?,?,?,?)";
    }

    @Override
    public void fillInsertStatementValues(PreparedStatement insertStatement) throws SQLException {
        insertStatement.setString(1, getID());
        insertStatement.setString(2, getName());
        insertStatement.setString(3, getHash());
        insertStatement.setString(4, getLocation());
        insertStatement.setString(5, getSpeechLocation());
        insertStatement.setBytes(6, DatabaseUtil.serializeObject(this));
    }

    @XmlTransient
    @Override
    public String getUpdateStatement() {
        return "SET Name=?, Location=?, SpeechLocation=?, Payload=? WHERE ID=?";
    }

    @Override
    public void fillUpdatetStatementValues(PreparedStatement insertStatement) throws SQLException {
        insertStatement.setString(1, getName());
        insertStatement.setString(2, getLocation());
        insertStatement.setString(3, getSpeechLocation());
        insertStatement.setBytes(4, DatabaseUtil.serializeObject(this));
        insertStatement.setString(5, getID());
    }

}
