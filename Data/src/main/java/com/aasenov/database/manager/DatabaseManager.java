package com.aasenov.database.manager;

import java.sql.PreparedStatement;

/**
 * This interface contain common operations that has to be used for database operations.
 */
public interface DatabaseManager {

    /**
     * Create table with given name.
     * 
     * @param tableName - name of table to create.
     * @param recreate - whether to delete old table if exists.
     */
    public void createTable(String tableName, String tableDeclaration, boolean recreate);

    /**
     * Retrieve number of rows for table with given name
     * 
     * @param tableName - name of table to check.
     * 
     * @return Number of rows that table has.
     */
    public int getNumRows(String tableName);

    /**
     * Construct prepared statement from given query.
     * 
     * @param query - query to use for statement constructing.
     * @return Constructed {@link PreparedStatement} object.
     */
    public PreparedStatement consturctPreparedStatement(String query);

    /**
     * Perform update operation.
     * 
     * @param statementToExecute - statement containing update query.
     * @return result from the update operation.
     */
    public int update(PreparedStatement statementToExecute);

    /**
     * Perform insert operation.
     * 
     * @param statementToExecute - statement containing insert query.
     * @return result from the insert operation.
     */
    int insert(PreparedStatement statementToExecute);

    /**
     * Cancel given statement and release acquired connection.
     * 
     * @param statementToCancel - statement to be canceled.
     */
    public void cancelStatement(PreparedStatement statementToCancel);

    /**
     * Close all opened resources.
     */
    public void close();
}
