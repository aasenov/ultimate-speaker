package com.aasenov.sqlite.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class SQLiteManager {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(SQLiteManager.class);

    private Connection mConnection;
    public SQLiteManager(String databaseFile) {
        // load JDBC driver
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            sLog.error(e.getMessage(), e);
            new RuntimeException(e);
        }

        try {
            // create a database connection
            mConnection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
        } catch (SQLException e) {
            sLog.error(e.getMessage(), e);
        }
    }

    public Connection getConnection() {
        return mConnection;
    }

    /**
     * Close all opened resources.
     */
    public void close() {
        if (mConnection != null) {
            try {
                mConnection.close();
            } catch (SQLException e) {
                sLog.error(e.getMessage(), e);
            }
        }
    }

}
