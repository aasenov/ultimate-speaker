package com.aasenov.database.manager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import javax.swing.SortOrder;

import org.apache.log4j.Logger;
import org.sqlite.core.PreparedStatementLogWrapper;
import org.sqlite.jdbc4.JDBC4PreparedStatement;

import com.aasenov.database.DatabaseUtil;
import com.aasenov.database.objects.DatabaseItem;

/**
 * Manager responsible for database operations. This class is singleton - thread safe.
 */
public class SQLiteManager implements DatabaseManager {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(SQLiteManager.class);

    /**
     * Static instance of given manager.
     */
    private static SQLiteManager sInstance;

    /**
     * Number of concurrent database connections.
     */
    private static int NUMBER_OPENED_DATABASE_CONNECTIONS = 5;

    /**
     * Queue with connections to database.
     */
    private Queue<Connection> mConnections = new LinkedList<Connection>();

    /**
     * Semaphore to keep track of available connections.
     */
    private Semaphore mConnectionsAvailable = new Semaphore(0);

    /**
     * File where database is stored.
     */
    private String mDatabaseFile;

    private SQLiteManager(String databaseFile) {
        mDatabaseFile = databaseFile;

        // load JDBC driver
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            sLog.error(e.getMessage(), e);
            new RuntimeException(e);
        }

        try {
            // create a database connections
            for (int i = 0; i < NUMBER_OPENED_DATABASE_CONNECTIONS; i++) {
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
                mConnections.add(connection);
                mConnectionsAvailable.release();
            }
        } catch (SQLException e) {
            sLog.error(e.getMessage(), e);
        }
    }

    /**
     * Retrieve static instance of this manager.
     * 
     * @param databaseFile - file to stora database tables.
     * 
     * @return Initialize {@link SQLiteManager} instance.
     */
    protected static synchronized SQLiteManager getInstance(String databaseFile) {
        if (sInstance == null) {
            sInstance = new SQLiteManager(databaseFile);
        }
        return sInstance;
    }

    /**
     * Clean statically initialized manager instnce.
     */
    protected static synchronized void destroy() {
        if (sInstance != null) {
            sInstance.close();
        }
        sInstance = null;
    }

    @Override
    public void createTable(String tableName, String tableDeclaration, String indexDeclaration) {
        String query = null;
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);// begin transaction

            query = String.format("CREATE TABLE IF NOT EXISTS %s %s", tableName, tableDeclaration);
            PreparedStatement pstm = createPreparedStatement(conn, query);
            pstm.execute();

            if (indexDeclaration != null && !indexDeclaration.isEmpty()) {
                String indexName = getTableIndex(tableName);
                query = String.format("CREATE INDEX IF NOT EXISTS %s ON %s (%s)", indexName, tableName,
                        indexDeclaration);
                pstm = createPreparedStatement(conn, query);
                pstm.execute();
            }

            conn.commit();// commit transaction
        } catch (Exception e) {
            sLog.error(String.format("Error executing query %s %s", query, e.getMessage()), e);
        } finally {
            if (conn != null) {
                releaseConnection(conn);
            }
        }
    }

    @Override
    public void deleteTable(String tableName) {
        String query = null;
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);// begin transaction

            query = String.format("DROP TABLE IF EXISTS '%s'", tableName);
            PreparedStatement pstm = createPreparedStatement(conn, query);
            pstm.execute();

            String indexName = getTableIndex(tableName);
            query = String.format("DROP INDEX IF EXISTS '%s'", indexName);
            pstm = createPreparedStatement(conn, query);
            pstm.execute();

            conn.commit();// commit transaction
        } catch (Exception e) {
            sLog.error(String.format("Error executing query %s %s", query, e.getMessage()), e);
        } finally {
            if (conn != null) {
                releaseConnection(conn);
            }
        }

    }

    @Override
    public void deleteAllTables() {
        // destroy static instance to force db file recreation on next use.
        destroy();

        // delete database file instead of deleting all tables
        File dbFile = new File(mDatabaseFile);
        if (dbFile.exists()) {
            sLog.info("Deleting file: " + dbFile.getAbsolutePath());
            dbFile.delete();
        }

    }

    @Override
    public int getNumRows(String tableName) {
        int result = 0;
        String query = null;
        Connection conn = null;
        try {
            conn = getConnection();

            query = String.format("SELECT COUNT(*) FROM %s", tableName);
            PreparedStatement pstm = createPreparedStatement(conn, query);
            ResultSet rs = pstm.executeQuery();
            if (rs != null && rs.next()) {
                result = rs.getInt(1);
            }
            pstm.close();
        } catch (Exception e) {
            sLog.error(String.format("Error executing query %s %s", query, e.getMessage()), e);
        } finally {
            if (conn != null) {
                releaseConnection(conn);
            }
        }

        return result;
    }

    @Override
    public <T extends DatabaseItem> void store(String tableName, Collection<T> items) {
        if (items != null && !items.isEmpty()) {
            Connection conn = null;
            try {
                conn = getConnection();
                conn.setAutoCommit(false);// start transaction

                for (T item : items) {
                    if (item.isForUpdate()) {
                        String updateStatement = String.format("UPDATE %s %s", tableName, item.getUpdateStatement());
                        PreparedStatement stmt = createPreparedStatement(conn, updateStatement);
                        item.fillUpdatetStatementValues(stmt);
                        stmt.execute();
                    } else {
                        String insertStatement = String.format("INSERT INTO %s %s", tableName,
                                item.getInsertStatement());
                        PreparedStatement stmt = createPreparedStatement(conn, insertStatement);
                        item.fillInsertStatementValues(stmt);
                        stmt.execute();
                    }
                }

                conn.commit(); // commit transaction
            } catch (Exception e) {
                sLog.error(e.getMessage(), e);
            } finally {
                if (conn != null) {
                    releaseConnection(conn);
                }
            }
        }
    }

    @Override
    public <T extends DatabaseItem> List<T> select(String tableName, String whereClause, int start, int count,
            String[] sortColumns, SortOrder sortOrder) {
        List<T> lst = new ArrayList<T>();

        // fix where clause
        if (whereClause == null) {
            whereClause = "";
        }

        // fix sorting
        String sortString = "";
        if (sortOrder != SortOrder.UNSORTED && sortColumns != null && sortColumns.length > 0) {
            sortString = String.format("ORDER BY %1$s %2$s", getSortColumnString(sortColumns),
                    getSortOrderString(sortOrder));
        }

        Connection conn = null;
        String query = null;
        try {
            conn = getConnection();

            query = String.format("SELECT * FROM %1$s %2$s %3$s LIMIT %4$s OFFSET %5$s", tableName, whereClause,
                    sortString, count, start);
            PreparedStatement sqlStatement = createPreparedStatement(conn, query);
            ResultSet rs = sqlStatement.executeQuery();
            while (rs.next()) {
                T rowObject = extractDatabaseItem(rs);
                if (rowObject != null) {
                    lst.add(rowObject);
                }
            }
            sqlStatement.close();
            return lst;
        } catch (Exception ex) {
            sLog.error("SQLiteWrapper.Select (exception): query: " + query, ex);
        } finally {
            if (conn != null) {
                releaseConnection(conn);
            }
        }
        return lst;
    }

    @Override
    public <T extends DatabaseItem> void delete(String tableName, String key) {
        Connection conn = null;
        String query = null;
        try {
            conn = getConnection();

            query = String.format("DELETE FROM %s WHERE ID IN (?)", tableName);
            PreparedStatement sqlStatement = createPreparedStatement(conn, query);
            sqlStatement.setString(1, key);
            sqlStatement.execute();
            sqlStatement.close();
        } catch (Exception ex) {
            sLog.error("SQLiteWrapper.Select (exception): query: " + query, ex);
        } finally {
            if (conn != null) {
                releaseConnection(conn);
            }
        }
    }

    @Override
    public void close() {
        sLog.info("Closing all connections.");

        for (Connection connection : mConnections) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    sLog.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Retrieve database connection from pool of available connections. Blocks until connection become available.
     * 
     * @return Connection to the databse.
     */
    private Connection getConnection() {
        try {
            mConnectionsAvailable.acquire();
        } catch (InterruptedException e) {
            sLog.error(e.getMessage(), e);
        }
        return mConnections.poll();
    }

    /**
     * Release given connection and put it back to the pool of available connections.
     * 
     * @param connectionToRelease - connection to be released.
     */
    private void releaseConnection(Connection connectionToRelease) {
        try {
            // reset autocommit flag.
            connectionToRelease.setAutoCommit(true);
        } catch (SQLException e) {
            sLog.error(e.getMessage(), e);
        }
        mConnections.add(connectionToRelease);
        mConnectionsAvailable.release();
    }

    /**
     * Construct name of index to be used for given table.
     * 
     * @param tableName - name of table to retrieve index of.
     * @return Constructed table index name.
     */
    private String getTableIndex(String tableName) {
        String indexName = String.format("%s_Idx", tableName);
        return indexName;
    }

    /**
     * Construct {@link PreparedStatement} wrapping original one into {@link PreparedStatementLogWrapper} instance to
     * improve logging.
     * 
     * @param conn - {@link Connection} to create prepared statement with.
     * @param query - query to prepare.
     * @return Wrapped {@link PreparedStatement} instance.
     * @throws SQLException in case of error.
     */
    private PreparedStatement createPreparedStatement(Connection conn, String query) throws SQLException {
        return new PreparedStatementLogWrapper((JDBC4PreparedStatement) conn.prepareStatement(query));
    }

    /**
     * Extract object from given result set.
     * 
     * @param rs - Result set to retrieve database object from.
     * @return Extracted object.
     * @throws Exception in case of error.
     */
    private <T extends DatabaseItem> T extractDatabaseItem(ResultSet rs) throws Exception {
        byte[] objectBytes = (byte[]) rs.getBytes("Payload");
        @SuppressWarnings("unchecked")
        T result = (T) DatabaseUtil.deserializeObject(objectBytes);
        result.setForUpdate(true);
        try {
            long rowid = rs.getLong("rowid");
            result.setRowID(rowid);
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
        }
        return result;
    }

    /**
     * Construct string for sorting from given column name.
     * 
     * @param sortColumns - array with columns to use for sorting.
     * @return Constructed string to use for query.
     */
    private String getSortColumnString(String[] sortColumns) {
        StringBuilder sb = new StringBuilder();
        for (String str : sortColumns) {
            sb.append(str);
            sb.append(",");
        }
        // remove last comma
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * Construct string for sorting from given {@link SortOrder} constant.
     * 
     * @param sortOrder - constant to transform.
     * @return Constructed string to use for query.
     */
    private String getSortOrderString(SortOrder sortOrder) {
        String order = "";
        switch (sortOrder) {
        case ASCENDING:
            order = "ASC";
            break;
        case DESCENDING:
        default:
            order = "DESC";
            break;
        }
        return order;
    }
}
