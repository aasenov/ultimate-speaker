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
}
