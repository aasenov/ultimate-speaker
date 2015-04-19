package com.aasenov.database.objects;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
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
     * List of items, currently loaded from databse.
     */
    private List<T> mItems = new ArrayList<T>();

    /**
     * Lock that is used to synchronize access to items.
     */
    private ReadWriteLock mReadWriteLock = new ReentrantReadWriteLock();

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
        mDatabaseManager = DatabaseProvider.getDefaultManager();
        mDatabaseManager.createTable(mTableName, getCreateTableProperties(), recreate);

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
            mItems.add(item);
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
            List<DatabaseItem> itemsToCommit = null;
            mReadWriteLock.writeLock().lock();
            try {
                sLog.info(String.format("Commiting %s number of objects. Total size before commit is %s.",
                        mItems.size(), size()));
                itemsToCommit = new ArrayList<DatabaseItem>();
                itemsToCommit.addAll(mItems);

                mItems.clear();
            } finally {
                mReadWriteLock.writeLock().unlock();
            }

            for (DatabaseItem itemToCommit : itemsToCommit) {
                if (itemToCommit.isForUpdate()) {
                    PreparedStatement stmt = null;
                    try {
                        String insertStatement = String.format("UPDATE %s SET %s", mTableName,
                                itemToCommit.getUpdateStatement());
                        stmt = mDatabaseManager.consturctPreparedStatement(itemToCommit.getUpdateStatement());
                        stmt.setString(1, mTableName);
                        itemToCommit.fillUpdatetStatementValues(stmt);
                        mDatabaseManager.update(stmt);
                    } catch (Exception ex) {
                        sLog.error(ex.getMessage(), ex);
                        mDatabaseManager.cancelStatement(stmt); // release connection
                    }
                } else {
                    PreparedStatement stmt = null;
                    try {
                        String insertStatement = String.format("INSERT INTO %s %s", mTableName,
                                itemToCommit.getInsertStatement());
                        stmt = mDatabaseManager.consturctPreparedStatement(insertStatement);
                        stmt.setString(1, mTableName);
                        itemToCommit.fillInsertStatementValues(stmt);
                        mDatabaseManager.insert(stmt);
                    } catch (Exception ex) {
                        sLog.error(ex.getMessage(), ex);
                        mDatabaseManager.cancelStatement(stmt); // release connection
                    }
                }
            }
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

}
