package com.aasenov.database.manager;

import java.util.Collection;
import java.util.List;

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
     * @param recreate - whether to delete old table if exists.
     */
    public void createTable(String tableName, String tableDeclaration, String indexDeclaration, boolean recreate);

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
     * Retrieve records from database table with given name, that satisfy where clause.
     * 
     * @param tableName - name of table to select from.
     * @param whereClause - where condition, to use for results refinement.
     * @return Records retrieved, or empty list if none.
     */
    public <T extends DatabaseItem> List<T> select(String tableName, String whereClause);

    /**
     * Close all opened resources.
     */
    public void close();
}
