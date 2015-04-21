package com.aasenov.database.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import com.aasenov.database.manager.DatabaseManager;
import com.aasenov.database.manager.DatabaseProvider;

/**
 * Object that represent database table. It contain list of items, each representing row from a table.
 */
public abstract class DatabaseTable<T extends DatabaseItem> {

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(DatabaseTable.class);

    /**
     * Number of records to keep in memory before commiting.
     */
    private static final int COMMIT_THRESHOLD = 100;

    /**
     * Mop of items, currently loaded in memory.
     */
    private Map<String, T> mItems;

    /**
     * Lock that is used to synchronize access to items.
     */
    private ReadWriteLock mReadWriteLock;

    /**
     * {@link DatabaseManager} instance to use to operate with the databse.
     */
    private DatabaseManager mDatabaseManager;

    /**
     * Name of current table.
     */
    private String mTableName;

    /**
     * Create instance of table with given name.
     * 
     * @param tableName - name of table to create.
     * @param recreate - whether to delete old table if exists.
     */
    public DatabaseTable(String tableName, boolean recreate) {
        mTableName = tableName;
        mItems = new HashMap<String, T>();
        mReadWriteLock = new ReentrantReadWriteLock();
        mDatabaseManager = DatabaseProvider.getDefaultManager();
        mDatabaseManager.createTable(mTableName, getCreateTableProperties(), getCreateTableIndexProperties(), recreate);

    }

    /**
     * Adds given item to the table.
     * 
     * @param item
     */
    public void add(T item) {
        commit(false); // commit to cleanup memory.
        mReadWriteLock.writeLock().lock();
        try {
            mItems.put(item.getID(), item);
        } finally {
            mReadWriteLock.writeLock().unlock();
        }
    }

    /**
     * Get total number of records stored in database. The result will not include records that are not commited yet.
     * 
     * @return Number of rows in database table.
     */
    public int size() {
        return mDatabaseManager.getNumRows(mTableName);
    }

    /**
     * Write objects from memory to database.
     * 
     * @param force - whether to force writing, or to wait for threshold to be exceeded.
     */
    public void commit(boolean force) {
        if (force || thresholdExceeded()) {
            Map<String, T> itemsToCommit = null;
            mReadWriteLock.writeLock().lock();
            try {
                sLog.info(String.format("Commiting %s number of objects. Total size before commit is %s.",
                        mItems.size(), size()));
                itemsToCommit = new HashMap<String, T>();
                itemsToCommit.putAll(mItems);

                mItems.clear();
            } finally {
                mReadWriteLock.writeLock().unlock();
            }

            mDatabaseManager.store(mTableName, itemsToCommit.values());
        }
    }

    /**
     * Checks whether commit threshold is exceeded.
     * 
     * @return <b>True</b> if the threshold is exceeded, <b>False</b> otherwise.
     */
    private boolean thresholdExceeded() {
        mReadWriteLock.readLock().lock();
        try {
            return mItems.size() > COMMIT_THRESHOLD;
        } finally {
            mReadWriteLock.readLock().unlock();
        }
    }

    /**
     * Retrieve create table properties, valid for current table instance.
     * 
     * @return Create table properties to use during table creation.
     */
    public abstract String getCreateTableProperties();

    /**
     * Retrieve create table index properties, valid for current table instance.
     * 
     * @return Create table index properties to use during table creation, if any.
     */
    public abstract String getCreateTableIndexProperties();

}
