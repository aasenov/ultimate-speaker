package com.aasenov.database.manager;

import com.aasenov.database.DatabaseType;

/**
 * Provider used to retrieve required {@link DatabaseManager} instances.
 */
public class DatabaseProvider {

    /**
     * Type of database to use.
     */
    private static DatabaseType sDatabaseType = DatabaseType.SQLite;

    /**
     * Getter for the {@link DatabaseProvider#sDatabaseType} field.
     *
     * @return the {@link DatabaseProvider#sDatabaseType} value.
     */
    public static DatabaseType getDatabaseType() {
        return sDatabaseType;
    }

    /**
     * Setter for the {@link DatabaseProvider#sDatabaseType} field.
     *
     * @param mDatabaseType the {@link DatabaseProvider#sDatabaseType} to set
     */
    public static void setDatabaseType(DatabaseType mDatabaseType) {
        DatabaseProvider.sDatabaseType = mDatabaseType;
    }

    /**
     * Retrieve default {@link DatabaseManager} instance.
     * 
     * @return Initialized database object.
     */
    public static DatabaseManager getDefaultManager() {
        switch (sDatabaseType) {
        case SQLite:
        default:
            return SQLiteManager.getInstance();
        }
    }
}
