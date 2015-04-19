package com.aasenov.database.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

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
    public void createTable(String tableName, String tableDeclaration, boolean recreate) {
        boolean tableExists = isTableExists(tableName);
        if (tableExists && recreate) {
            // delete table
            deleteTable(tableName);
            tableExists = false;
        }

        if (!tableExists) {
            PreparedStatement statement = null;
            try {
                statement = consturctPreparedStatement(String.format("CREATE TABLE IF NOT EXISTS %s %s", tableName,
                        tableDeclaration));
                execute(statement);
            } catch (Exception e) {
                sLog.error(e.getMessage(), e);
                if (statement != null) {
                    cancelStatement(statement);
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
        PreparedStatement statement = null;
        try {
            statement = consturctPreparedStatement("SELECT name FROM sqlite_master WHERE type='table' AND name=?");
            statement.setString(1, tableName);
            ResultSet rs = executeQuery(statement);
            if (rs != null) {
                try {
                    result = rs.next();
                    while (rs.next()) {
                        // read all result
                    }
                } catch (SQLException e) {
                    sLog.error(e.getMessage(), e);
                }
            }
        } catch (SQLException e) {
            sLog.error(e.getMessage(), e);
            if (statement != null) {
                cancelStatement(statement);
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
        PreparedStatement statement = null;
        try {
            statement = consturctPreparedStatement("DROP TABLE IF EXISTS " + tableName);
            execute(statement);
        } catch (Exception e) {
            sLog.error(e.getMessage(), e);
            if (statement != null) {
                cancelStatement(statement);
            }
        }
    }

    /**
     * Execute database query. Used for Create/Drop statements.
     * 
     * @param statementToExecute - query to execute.
     */
    private void execute(PreparedStatement statementToExecute) {
        Connection connection = null;
        try {
            connection = statementToExecute.getConnection();
            statementToExecute.execute();
            statementToExecute.close();

            if (sLog.isDebugEnabled()) {
                sLog.debug(String.format("Executing: %s.", statementToExecute));
            }
        } catch (SQLException e) {
            sLog.error(e.getMessage(), e);
        } finally {
            if (connection != null) {
                releaseConnection(connection);
            }
        }

    }

    /**
     * Execute database query. Used for select statements.
     * 
     * @param statementToExecute - statement to execute.
     * @return Result of the query.
     */
    private ResultSet executeQuery(PreparedStatement statementToExecute) {
        ResultSet result = null;
        Connection connection = null;
        try {
            connection = statementToExecute.getConnection();
            result = statementToExecute.executeQuery();

            if (sLog.isDebugEnabled()) {
                sLog.debug(String.format("Executing query: %s.", statementToExecute));
            }
        } catch (SQLException e) {
            sLog.error(e.getMessage(), e);
        } finally {
            if (connection != null) {
                releaseConnection(connection);
            }
        }
        return result;
    }

    /**
     * Execute database query. Used for Insert/Update/Delete statements.
     * 
     * @param statementToExecute - statement to execute.
     * @return Result of the query.
     */
    private int executeUpdate(PreparedStatement statementToExecute) {
        int result = 0;
        Connection connection = null;
        try {
            connection = statementToExecute.getConnection();
            result = statementToExecute.executeUpdate();
            statementToExecute.close();

            if (sLog.isDebugEnabled()) {
                sLog.debug(String.format("Executing update query: %s result %s", statementToExecute, result));
            }
        } catch (SQLException e) {
            sLog.error(e.getMessage(), e);
        } finally {
            if (connection != null) {
                releaseConnection(connection);
            }
        }
        return result;
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

    @Override
    public int getNumRows(String tableName) {
        int result = 0;
        PreparedStatement statement = null;
        try {
            statement = consturctPreparedStatement("SELECT Count(*) FROM " + tableName);
            ResultSet rs = executeQuery(statement);
            if (rs != null) {
                try {
                    if (rs.next()) {
                        result = rs.getInt(1);
                    }
                } catch (SQLException e) {
                    sLog.error(e.getMessage(), e);
                }
            }
            statement.close();
        } catch (Exception e) {
            sLog.error(e.getMessage(), e);
            if (statement != null) {
                cancelStatement(statement);
            }
        }

        return result;
    }

    @Override
    public PreparedStatement consturctPreparedStatement(String query) {
        Connection conn = null;
        try {
            conn = getConnection();
            return conn.prepareStatement(query);
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
            if (conn != null) {
                // return connection for reuse in case of error.
                releaseConnection(conn);
            }
        }
        return null;
    }

    @Override
    public int update(PreparedStatement statementToExecute) {
        return executeUpdate(statementToExecute);
    }

    @Override
    public int insert(PreparedStatement statementToExecute) {
        return executeUpdate(statementToExecute);

    }

    @Override
    public void cancelStatement(PreparedStatement statementToCancel) {
        try {
            releaseConnection(statementToCancel.getConnection());
            statementToCancel.close();
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
        }
    }

}
