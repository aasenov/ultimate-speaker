package com.aasenov.database.objects;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aasenov.database.DatabaseUtil;
import com.aasenov.database.manager.DatabaseProvider;

@XmlRootElement(name = "UserFileRelationItem")
@XmlType(name = "UserFileRelationItem")
public class UserFileRelationItem extends DatabaseItem {
    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default name of table, containing these items.
     */
    public static String DEFAULT_TABLE_NAME = "UserFileRelation";

    /**
     * Name of column used to store user IDs.
     */
    public static String COLUMN_USER_ID = "UserID";

    /**
     * Name of column used to store file IDs.
     */
    public static String COLUMN_FILE_ID = "FileID";

    private String mUserID;
    private String mFileID;

    /**
     * Do not use, as this is the key of this object!!!
     */
    @Deprecated
    public UserFileRelationItem() {
        super();
    }

    public UserFileRelationItem(String userID, String fileID) {
        super(String.format("%s_%s", userID, fileID));
        mUserID = userID;
        mFileID = fileID;
    }

    /**
     * Getter for the {@link UserFileRelationItem#mUserID} property.
     * 
     * @return the {@link UserFileRelationItem#mUserID}
     */
    @XmlElement(name = "UserID")
    public String getUserID() {
        return mUserID;
    }

    /**
     * Setter for the {@link UserFileRelationItem#mUserID} property. Do not use, as this is the key of this object!!!
     * 
     * @param mUserID the {@link UserFileRelationItem#mUserID} to set
     */
    @Deprecated
    public void setUserID(String mUserID) {
        this.mUserID = mUserID;
    }

    /**
     * Getter for the {@link UserFileRelationItem#mFileID} property.
     * 
     * @return the {@link UserFileRelationItem#mFileID}
     */
    @XmlElement(name = "FileID")
    public String getFileID() {
        return mFileID;
    }

    /**
     * Setter for the {@link UserFileRelationItem#mFileID} property. Do not use, as this is the key of this object!!!
     * 
     * @param mFileID the {@link UserFileRelationItem#mFileID} to set
     */
    @Deprecated
    public void setFileID(String mFileID) {
        this.mFileID = mFileID;
    }

    @XmlTransient
    @Override
    public String getDatabaseTableProperties() {
        switch (DatabaseProvider.getDatabaseType()) {
        case SQLite:
        default:
            return String
                    .format("(rowid INTEGER PRIMARY KEY NOT NULL, ID TEXT UNIQUE NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, Payload BLOB)",
                            COLUMN_USER_ID, COLUMN_FILE_ID);
        }
    }

    @XmlTransient
    @Override
    public String getIndexColumns() {
        switch (DatabaseProvider.getDatabaseType()) {
        case SQLite:
        default:
            return String.format("%s, %s", COLUMN_USER_ID, COLUMN_FILE_ID);
        }
    }

    @Override
    public String toString() {
        return String.format("%s userID=%s fileID=%s", super.toString(), getUserID(), getFileID());
    }

    @XmlTransient
    @Override
    public String getInsertStatement() {
        // skip rowID, it will be generated automatically.
        return String.format("(ID, %s, %s, Payload) VALUES (?,?,?,?)", COLUMN_USER_ID, COLUMN_FILE_ID);
    }

    @Override
    public void fillInsertStatementValues(PreparedStatement insertStatement) throws SQLException {
        insertStatement.setString(1, getID());
        insertStatement.setString(2, getUserID());
        insertStatement.setString(3, getFileID());
        insertStatement.setBytes(4, DatabaseUtil.serializeObject(this));
    }

    @XmlTransient
    @Override
    public String getUpdateStatement() {
        throw new RuntimeException("This object doesn't supprot update statement.");
    }

    @Override
    public void fillUpdatetStatementValues(PreparedStatement insertStatement) throws SQLException {
        throw new RuntimeException("This object doesn't supprot update statement.");
    }
}
