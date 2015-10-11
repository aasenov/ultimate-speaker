package com.aasenov.database.provider;

import java.io.File;

import com.aasenov.database.DatabaseManager;
import com.aasenov.database.manager.sqlite.SQLiteManager;
import com.aasenov.helper.ConfigHelper;
import com.aasenov.helper.ConfigProperty;

/**
 * Provider used to retrieve required {@link DatabaseManager} instances.
 */
public class DatabaseProvider {

    /**
     * Name of file under which database will be stored.
     */
    private static final String DATABASE_FILE_NAME = "ultimateSpeaker.db";

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
            File dbFile = new File(ConfigHelper.getInstance().getConfigPropertyValue(ConfigProperty.StorageDir),
                    DATABASE_FILE_NAME);
            return SQLiteManager.getInstance(dbFile.getAbsolutePath());
        }
    }

    /**
     * Destroy static instance of all managers.
     */
    public static void destroyManagers() {
        SQLiteManager.destroy();
    }
}
