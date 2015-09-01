package com.aasenov.database.objects;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aasenov.database.DatabaseUtil;
import com.aasenov.database.manager.DatabaseProvider;

@XmlRootElement(name = "UserItem")
@XmlType(name = "UserItem")
public class UserItem extends DatabaseItem {
    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default name of table, containing these items.
     */
    public static String DEFAULT_TABLE_NAME = "Users";

    private String mName;
    private String mPassword;
    private String mEmail;

    /**
     * Do not use, as this is the key of this object!!!
     */
    @Deprecated
    protected UserItem() {
        super();
    }

    public UserItem(String email) {
        super(email);
        mEmail = email;
    }

    public UserItem(String name, String password, String email) {
        this(email);
        mName = name;
        mPassword = password;
    }

    /**
     * Getter for the {@link UserItem#mName} field.
     * 
     * @return the {@link UserItem#mName} value.
     */
    @XmlElement(name = "Name")
    public String getName() {
        return mName;
    }

    /**
     * Setter for the {@link UserItem#mName} field.
     * 
     * @param name the {@link UserItem#mName} to set
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Getter for the {@link UserItem#mPassword} property.
     * 
     * @return the {@link UserItem#mPassword}
     */
    @XmlElement(name = "Password")
    public String getPassword() {
        return mPassword;
    }

    /**
     * Setter for the {@link UserItem#mPassword} property
     * 
     * @param password the {@link UserItem#mPassword} to set
     */
    public void setPassword(String password) {
        mPassword = password;
    }

    /**
     * Getter for the {@link UserItem#mEmail} property.
     * 
     * @return the {@link UserItem#mEmail}
     */
    @XmlElement(name = "Email")
    public String getEmail() {
        return mEmail;
    }

    /**
     * Setter for the {@link UserItem#mEmail} property. Do not use, as this is the key of this object!!!
     * 
     * @param email the {@link UserItem#mEmail} to set
     */
    @Deprecated
    public void setEmail(String email) {
        mEmail = email;
        setID(email);
    }

    @XmlTransient
    @Override
    public String getDatabaseTableProperties() {
        switch (DatabaseProvider.getDatabaseType()) {
        case SQLite:
        default:
            return "(rowid INTEGER PRIMARY KEY NOT NULL, ID TEXT UNIQUE NOT NULL, Name TEXT NOT NULL,Password TEXT NOT NULL, Email TEXT UNIQUE NOT NULL, Payload BLOB)";
        }
    }

    @XmlTransient
    @Override
    public String getIndexColumns() {
        switch (DatabaseProvider.getDatabaseType()) {
        case SQLite:
        default:
            return "ID, Email";
        }
    }

    @Override
    public String toString() {
        return String.format("%s name=%s password=%s email=%s", super.toString(), getName(), getPassword(), getEmail());
    }

    @XmlTransient
    @Override
    public String getInsertStatement() {
        // skip rowID, it will be generated automatically.
        return "(ID, Name, Password, Email, Payload) VALUES (?,?,?,?,?)";
    }

    @Override
    public void fillInsertStatementValues(PreparedStatement insertStatement) throws SQLException {
        insertStatement.setString(1, getID());
        insertStatement.setString(2, getName());
        insertStatement.setString(3, getPassword());
        insertStatement.setString(4, getEmail());
        insertStatement.setBytes(5, DatabaseUtil.serializeObject(this));
    }

    @XmlTransient
    @Override
    public String getUpdateStatement() {
        return "SET Name=?, Password=?, Payload=? WHERE ID=?";
    }

    @Override
    public void fillUpdatetStatementValues(PreparedStatement insertStatement) throws SQLException {
        insertStatement.setString(1, getName());
        insertStatement.setString(2, getPassword());
        insertStatement.setBytes(3, DatabaseUtil.serializeObject(this));
        insertStatement.setString(4, getID());
    }

}
