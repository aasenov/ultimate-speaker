package com.aasenov.database.manager;

import java.util.Collection;
import java.util.List;

import javax.swing.SortOrder;

import com.aasenov.database.objects.DatabaseItem;

/**
 * This interface contain common operations that has to be used for database operations.
 */
public interface DatabaseManager {

    /**
     * Create table with given name.
     * 
     * @param tableName - name of table to create.
     * @param tableDeclaration - table declaration parameters to use.
     * @param indexDeclaration - index declaration parameters to use, if any.
     */
    public void createTable(String tableName, String tableDeclaration, String indexDeclaration);

    /**
     * Delete table with given name.
     * 
     * @param tableName - name of table to create.
     */
    public void deleteTable(String tableName);

    /**
     * Delete all tables.
     */
    public void deleteAllTables();

    /**
     * Retrieve number of rows for table with given name
     * 
     * @param tableName - name of table to check.
     * 
     * @return Number of rows that table has.
     */
    public int getNumRows(String tableName);

    /**
     * IStore given list in database. Insert given items or update them if they already exists.
     * 
     * @param tableName - name of table to store items to.
     * @param items - Items to be stored.
     */
    public <T extends DatabaseItem> void store(String tableName, Collection<T> items);

    /**
     * Retrieve records from database table with given name, that satisfy where clause. Use given paging and sorting
     * properties.
     * 
     * @param tableName - name of table to select from.
     * @param whereClause - where condition, to use for results refinement.
     * @param start - from where to start the page.
     * @param count - number of results to return.
     * @param sortOrder - Order of sorting
     * @param sortColumns - columns to sort on.
     * @return Records retrieved, or empty list if none.
     */
    public <T extends DatabaseItem> List<T> select(String tableName, String whereClause, int start, int count,
            String[] sortColumns, SortOrder sortOrder);

    /**
     * Removes item with given key from database.
     * 
     * @param tableName - name of table to delete from.
     * @param key - key of item to be deleted.
     */
    public <T extends DatabaseItem> void delete(String tableName, String key);

    /**
     * Close all opened resources.
     */
    public void close();
}
