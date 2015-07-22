package com.aasenov.database.objects;

import java.util.Arrays;
import java.util.List;

import javax.swing.SortOrder;

import org.apache.log4j.Logger;

import com.aasenov.database.WhereClauseManager;
import com.aasenov.database.WhereClauseParameter;
import com.aasenov.database.manager.DatabaseProvider;

/**
 * Object that represent database table.
 */
public class DatabaseTable<T extends DatabaseItem> {

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(DatabaseTable.class);

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
        DatabaseProvider.getDefaultManager().createTable(mTableName, getCreateTableProperties(),
                getCreateTableIndexProperties());
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
        DatabaseProvider.getDefaultManager().store(mTableName, Arrays.asList(items));
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

            List<T> lst = DatabaseProvider.getDefaultManager().<T> select(mTableName, whereClauseManager.toString(), 0,
                    1, null, null);
            if (lst != null && !lst.isEmpty()) {
                res = lst.get(0);
            }
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
        }
        return res;
    }

    /**
     * Retrieve all objects from given collection - select from database.
     * 
     * @param keys - keys of item to retrieve.
     * @return Objects found in database file, <b>Null</b> if none.
     */
    public List<T> getAll(List<String> keys) {
        List<T> res = null;
        try {
            // select from databse
            WhereClauseManager whereClauseManager = new WhereClauseManager();
            for (String key : keys) {
                whereClauseManager.getOrCollection().add(new WhereClauseParameter("ID", key));
            }

            List<T> lst = DatabaseProvider.getDefaultManager().<T> select(mTableName, whereClauseManager.toString(), 0,
                    keys.size(), null, null);
            if (lst != null) {
                res = lst;
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

        return DatabaseProvider.getDefaultManager().select(mTableName, null, start, count, sortColumns, sortOrder);
    }

    /**
     * Remove object with given key from memory and from database if exists
     * 
     * @param key - key of item to remove.
     */
    public void remove(String key) {
        DatabaseProvider.getDefaultManager().delete(mTableName, key);
    }

    /**
     * Get total number of records stored in database. The result will not include records that are not commited yet.
     * 
     * @return Number of rows in database table.
     */
    public int size() {
        return DatabaseProvider.getDefaultManager().getNumRows(mTableName, "");
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

    /**
     * Get name of this database table.
     * 
     * @return Name of table, provided during initialization.
     */
    public String getTableName() {
        return mTableName;
    }

}
