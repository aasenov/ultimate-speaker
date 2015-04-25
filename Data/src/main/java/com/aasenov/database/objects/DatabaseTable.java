package com.aasenov.database.objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.SortOrder;

import org.apache.log4j.Logger;

import com.aasenov.database.WhereClauseManager;
import com.aasenov.database.WhereClauseParameter;
import com.aasenov.database.manager.DatabaseManager;
import com.aasenov.database.manager.DatabaseProvider;

/**
 * Object that represent database table. It contain list of items, each representing row from a table. This table
 * implement some caching, as used items are loaded in memory and retrieved from there
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
     * Adds given item to the table. This value will be kept in memory till calling
     * {@link DatabaseTable#commit(boolean)} method.
     * 
     * @param item - item to add.
     */
    public void add(T item) {
        mReadWriteLock.writeLock().lock();
        try {
            mItems.put(item.getID(), item);
        } finally {
            mReadWriteLock.writeLock().unlock();
        }
    }

    /**
     * Retrieve object from given collection. First try from objects that are in memory. If not found try to load one
     * from cache file.
     * 
     * @param key - key of item to retrieve.
     * @return Object found either in memory or in cache file, <b>Null</b> if none.
     */
    public T get(String key) {
        T res = null;
        try {
            // try from memory
            res = getLocal(key);

            // select from databse
            if (res == null) {
                WhereClauseManager whereClauseManager = new WhereClauseManager();
                whereClauseManager.getAndCollection().add(new WhereClauseParameter("ID", key));

                List<T> lst = mDatabaseManager.<T> select(mTableName, whereClauseManager.toString(), 0, 1, null, null);
                if (lst != null && !lst.isEmpty()) {
                    res = lst.get(0);
                }

                if (res != null) {
                    add(res);
                }
            }
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
        }
        return res;
    }

    /**
     * Retrieve object that is loaded in memory. If not found do not load one from database file.
     * 
     * @param key - key of item to retrieve.
     * @return Object that is load in memory, <b>null</b> otherwise.
     */
    public T getLocal(String key) {
        try {
            T res = null;
            mReadWriteLock.readLock().lock();
            try {
                res = mItems.get(key);
            } finally {
                mReadWriteLock.readLock().unlock();
            }

            return res;
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * Retrieve given number of objects from database, starting from given point. Before retrieving, all changes to
     * database are committed.
     * 
     * @param start - from where to start the page.
     * @param count - number of results to return.
     * @return Resulting page.
     */
    public List<T> getPage(int start, int count) {
        return getPage(start, count, SortOrder.UNSORTED, null);
    }

    /**
     * Retrieve given number of objects from database, starting from given point using given sorting preferences. Before
     * retrieving, all changes to database are committed.
     * 
     * @param start - from where to start the page.
     * @param count - number of results to return.
     * @param sortOrder - Order of sorting
     * @param sortColumns - columns to sort on.
     * @return Resulting sorted page.
     */
    public List<T> getPage(int start, int count, SortOrder sortOrder, String[] sortColumns) {
        // commit before loading given page.
        commit(true);

        // fix sort order.
        if (sortOrder == null) {
            sortOrder = SortOrder.UNSORTED;
        }

        return mDatabaseManager.select(mTableName, null, start, count, sortColumns, sortOrder);
    }

    /**
     * Remove object with given key from memory and from database if exists
     * 
     * @param key - key of item to remove.
     */
    public void remove(String key) {
        try {
            mReadWriteLock.readLock().lock();
            try {
                mItems.remove(key);
            } finally {
                mReadWriteLock.readLock().unlock();
            }

            mDatabaseManager.delete(mTableName, key);
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
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
                if (mItems.isEmpty()) {
                    sLog.info("Nothing to commit!");
                    return;
                }

                if (sLog.isDebugEnabled()) {
                    sLog.debug(String.format("Commiting %s number of objects. Total size before commit is %s.",
                            mItems.size(), size()));
                } else {
                    sLog.info(String.format("Commiting %s number of objects.", mItems.size()));

                }
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
