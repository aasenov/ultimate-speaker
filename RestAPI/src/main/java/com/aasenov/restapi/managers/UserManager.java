package com.aasenov.restapi.managers;

import org.apache.log4j.Logger;

import com.aasenov.database.objects.DatabaseTable;
import com.aasenov.database.objects.UserItem;

/**
 * Managers used to handle operations with users
 */
public class UserManager {

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(UserManager.class);

    /**
     * Database table containing users.
     */
    private DatabaseTable<UserItem> mUsersTable;

    /**
     * Static instance of this class.
     */
    private static UserManager sInstance;

    /**
     * Retrieve static instance of this manager.
     * 
     * @return Initialized {@link UserManager} object.
     */
    public static synchronized UserManager getInstance() {
        if (sInstance == null) {
            sInstance = new UserManager();
        }
        return sInstance;
    }

    /**
     * Destroy statically initialize instance of this class.
     */
    public static synchronized void destroy() {
        sInstance = null;
    }

    /**
     * Initialize manager.
     */
    private UserManager() {
        mUsersTable = new DatabaseTable<UserItem>(UserItem.DEFAULT_TABLE_NAME, new UserItem(null));
    }

    /**
     * Check whether user with given ID exists.
     * 
     * @param userID - id of user to check.
     * @return <b>True</b> if user exists, <b>False</b> otherwise.
     */
    public boolean checkUserExists(String userID) {
        if (userID != null && !userID.isEmpty()) {
            return mUsersTable.get(userID) != null;
        }
        return false;
    }

    /**
     * Retrieve user with given ID.
     * 
     * @param userID - id of user to retrieve.
     * @return User retrieved, or <b>Null</b> if none.
     */
    public UserItem getUser(String userID) {
        if (userID != null && !userID.isEmpty()) {
            return mUsersTable.get(userID);
        }
        return null;
    }

    /**
     * Store given user in database.
     * 
     * @param newUser - user to store.
     * @return <b>True</b> if user creation was successful, <b>False</b> otherwise.
     */
    public boolean createUser(UserItem newUser) {
        try {
            mUsersTable.add(newUser);
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
            return false;
        }
        return true;
    }
}
