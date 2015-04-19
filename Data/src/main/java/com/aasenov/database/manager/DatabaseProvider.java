package com.aasenov.database.manager;

import com.aasenov.database.DatabaseType;

/**
 * Provider used to retrieve required {@link DatabaseManager} instances.
 */
public class DatabaseProvider {

    private static final DatabaseType DEFAULT_PROVIDER = DatabaseType.SQLite;

    /**
     * Retrieve default {@link DatabaseManager} instance.
     * 
     * @return Initialized database object.
     */
    public static DatabaseManager getDefaultManager() {
        switch (DEFAULT_PROVIDER) {
        case SQLite:
        default:
            return SQLiteManager.getInstance();
        }
    }
}
