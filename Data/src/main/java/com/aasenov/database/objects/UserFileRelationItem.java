package com.aasenov.database.objects;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aasenov.database.DatabaseUtil;
import com.aasenov.database.err.NotInRangeException;
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

    /**
     * Name of column used to store file rating.
     */
    public static String COLUMN_RATING = "RATING";

    /**
     * File rating minimum value.
     */
    public static final int RATING_MIN_VALUE = 0;

    /**
     * File rating maximum value.
     */
    public static final int RATING_MAX_VALUE = 10;

    private String mUserID;
    private String mFileID;
    private double mRating;

    /**
     * Do not use, as this is the key of this object!!!
     */
    @Deprecated
    protected UserFileRelationItem() {
        super();
    }

    public UserFileRelationItem(String userID, String fileID) {
        super(String.format("%s_%s", userID, fileID));
        mUserID = userID;
        mFileID = fileID;
        try {
            setRating(0);
        } catch (NotInRangeException e) {
            // skip
        }
    }

    public UserFileRelationItem(String userID, String fileID, double rating) throws NotInRangeException {
        this(userID, fileID);
        setRating(rating);
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

    /**
     * Getter for the {@link UserFileRelationItem#mRating} property.
     * 
     * @return the {@link UserFileRelationItem#mRating}
     */
    @XmlElement(name = "Rating")
    public double getRating() {
        return mRating;
    }

    /**
     * Setter for the {@link UserFileRelationItem#mRating} property
     * 
     * @param rating the {@link UserFileRelationItem#mRating} to set
     * @throws NotInRangeException - in case of value not in range [{@link UserFileRelationItem#RATING_MIN_VALUE}:
     *             {@link UserFileRelationItem#RATING_MAX_VALUE}]
     */
    public void setRating(double rating) throws NotInRangeException {
        if (Math.floor(rating) < RATING_MIN_VALUE || Math.ceil(rating) > RATING_MAX_VALUE) {
            throw new NotInRangeException(String.format("Specified rating %s is not in bounds [%s:%s]", rating,
                    RATING_MIN_VALUE, RATING_MAX_VALUE));
        }
        mRating = rating;
    }

    @XmlTransient
    @Override
    public String getDatabaseTableProperties() {
        switch (DatabaseProvider.getDatabaseType()) {
        case SQLite:
        default:
            return String
                    .format("(rowid INTEGER PRIMARY KEY NOT NULL, ID TEXT UNIQUE NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s REAL, Payload BLOB)",
                            COLUMN_USER_ID, COLUMN_FILE_ID, COLUMN_RATING);
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
        return String.format("%s userID=%s fileID=%s rating=%s", super.toString(), getUserID(), getFileID(),
                getRating());
    }

    @XmlTransient
    @Override
    public String getInsertStatement() {
        // skip rowID, it will be generated automatically.
        return String.format("(ID, %s, %s, %s, Payload) VALUES (?,?,?,?,?)", COLUMN_USER_ID, COLUMN_FILE_ID,
                COLUMN_RATING);
    }

    @Override
    public void fillInsertStatementValues(PreparedStatement insertStatement) throws SQLException {
        insertStatement.setString(1, getID());
        insertStatement.setString(2, getUserID());
        insertStatement.setString(3, getFileID());
        insertStatement.setDouble(4, getRating());
        insertStatement.setBytes(5, DatabaseUtil.serializeObject(this));
    }

    @XmlTransient
    @Override
    public String getUpdateStatement() {
        return String.format("SET %s=? WHERE ID=?", COLUMN_RATING);
    }

    @Override
    public void fillUpdatetStatementValues(PreparedStatement updateStatement) throws SQLException {
        updateStatement.setDouble(1, getRating());
        updateStatement.setString(2, getID());
    }
}
