package com.aasenov.database.manager;

import java.util.Collection;

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
     * Perform update operation.
     * 
     * @param statementToExecute - statement containing update query.
     * @return result from the update operation.
     */
    /**
     * IStore given list in database. Insert given items or update them if they already exists.
     * 
     * @param tableName - name of table to store items to.
     * @param items - Items to be stored.
     */
    public <T extends DatabaseItem> void store(String tableName, Collection<T> items);

    /**
     * Close all opened resources.
     */
    public void close();
}
