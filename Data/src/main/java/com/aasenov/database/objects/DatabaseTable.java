package com.aasenov.database.objects;

import java.util.Arrays;
import java.util.List;

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
public class DatabaseTable<T extends DatabaseItem> {

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(DatabaseTable.class);

    /**
     * {@link DatabaseManager} instance to use to operate with the databse.
     */
    private DatabaseManager mDatabaseManager;

    /**
     * Name of current table.
     */
    private String mTableName;

    /**
     * Object used to call common methods.
     */
    private T mRowObject;

    /**
     * Create instance of table with given name.
     * 
     * @param tableName - name of table to create.
     * @param rowObject - Object of generic type T, that will be used to access methods, not particularly bind to object
     *            instance, but to class, cause it's not possible to overrite static methods.
     */
    public DatabaseTable(String tableName, T rowObject) {
        mTableName = tableName;
        mRowObject = rowObject;
        mDatabaseManager = DatabaseProvider.getDefaultManager();
        mDatabaseManager.createTable(mTableName, getCreateTableProperties(), getCreateTableIndexProperties());
    }

    /**
     * Adds given item to the table.
     * 
     * @param item - item to add.
     */
    public void add(T item) {
        addAll(item);
    }

    /**
     * Adds given items to the table.
     * 
     * @param itemss - item/s to add.
     */
    @SafeVarargs
    public final void addAll(T... items) {
        mDatabaseManager.store(mTableName, Arrays.asList(items));
    }

    /**
     * Retrieve object from given collection - select from database.
     * 
     * @param key - key of item to retrieve.
     * @return Object found in database file, <b>Null</b> if none.
     */
    public T get(String key) {
        T res = null;
        try {
            // select from databse
            WhereClauseManager whereClauseManager = new WhereClauseManager();
            whereClauseManager.getAndCollection().add(new WhereClauseParameter("ID", key));

            List<T> lst = mDatabaseManager.<T> select(mTableName, whereClauseManager.toString(), 0, 1, null, null);
            if (lst != null && !lst.isEmpty()) {
                res = lst.get(0);
            }
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
        }
        return res;
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
        mDatabaseManager.delete(mTableName, key);
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
     * Retrieve create table properties, valid for current table instance.
     * 
     * @return Create table properties to use during table creation.
     */
    public String getCreateTableProperties() {
        return mRowObject.getDatabaseTableProperties();
    }

    /**
     * Retrieve create table index properties, valid for current table instance.
     * 
     * @return Create table index properties to use during table creation, if any.
     */
    public String getCreateTableIndexProperties() {
        return mRowObject.getIndexColumns();
    }

}
