package com.aasenov.database.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;

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
     * Database file to use.
     */
    private static String DATABASE_FILE = "simple.db";

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

    private SQLiteManager(String databaseFile) {
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
     * @return Initialize {@link SQLiteManager} instance.
     */
    protected static synchronized SQLiteManager getInstance() {
        if (sInstance == null) {
            sInstance = new SQLiteManager(DATABASE_FILE);
        }
        return sInstance;
    }

    @Override
    public void createTable(String tableName, String tableDeclaration, String indexDeclaration, boolean recreate) {
        boolean tableExists = isTableExists(tableName);
        if (tableExists && recreate) {
            // delete table
            deleteTable(tableName);
            tableExists = false;
        }

        if (!tableExists) {
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
                conn.setAutoCommit(true);
            } catch (Exception e) {
                sLog.error(String.format("Error executing query %s %s", query, e.getMessage()), e);
            } finally {
                if (conn != null) {
                    releaseConnection(conn);
                }
            }
        } else if (sLog.isDebugEnabled()) {
            sLog.debug("Table exists. Skip creation.");
        }
    }

    /**
     * Checks weather table with given name already exists.
     * 
     * @param tableName - name of table to create.
     * @return <b>True</b> if table already exists, <b>False</b> otherwise.
     */
    private boolean isTableExists(String tableName) {
        boolean result = false;
        String query = null;
        Connection conn = null;
        try {
            conn = getConnection();

            query = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
            PreparedStatement pstm = createPreparedStatement(conn, query);
            pstm.setString(1, tableName);
            ResultSet rs = pstm.executeQuery();
            if (rs != null && rs.next()) {
                String name = rs.getString("name");
                if (name != null && !name.isEmpty()) {
                    result = true;
                }
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

    /**
     * Delete table with given name.
     * 
     * @param tableName - name of table to delete.
     */
    private void deleteTable(String tableName) {
        String query = null;
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);// begin transaction

            query = String.format("DROP TABLE IF EXISTS '%s'", tableName);
            PreparedStatement pstm = createPreparedStatement(conn, query);
            ;
            pstm.execute();

            String indexName = getTableIndex(tableName);
            query = String.format("DROP INDEX IF EXISTS '%s'", indexName);
            pstm = createPreparedStatement(conn, query);
            ;
            pstm.execute();

            conn.commit();// commit transaction
            conn.setAutoCommit(true);
        } catch (Exception e) {
            sLog.error(String.format("Error executing query %s %s", query, e.getMessage()), e);
        } finally {
            if (conn != null) {
                releaseConnection(conn);
            }
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
            ;
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
                conn.setAutoCommit(true);
            } catch (Exception e) {
                sLog.error(e.getMessage(), e);
            } finally {
                if (conn != null) {
                    releaseConnection(conn);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DatabaseItem> List<T> select(String tableName, String whereClause) {
        List<T> lst = new ArrayList<T>();
        Connection conn = null;
        String query = null;
        try {
            conn = getConnection();

            query = String.format("SELECT * FROM %s %s", tableName, whereClause);
            PreparedStatement sqlStatement = createPreparedStatement(conn, query);
            ResultSet result = sqlStatement.executeQuery();
            while (result.next()) {
                T rowObject = this.<T> extractDatabaseItem(result);
                if (rowObject != null) {
                    lst.add(rowObject);
                }
            }

            return lst;
        } catch (Exception ex) {
            sLog.error("SQLiteWrapper.Select (exception): query: " + query, ex);
        } finally {
            if (conn != null) {
                releaseConnection(conn);
            }
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public void close() {
        sLog.info("Closeing all connections.");

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
}
