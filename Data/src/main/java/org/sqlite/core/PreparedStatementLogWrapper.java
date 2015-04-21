package org.sqlite.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.sqlite.jdbc4.JDBC4PreparedStatement;

/**
 * This class is used to more easily log database commands. It's purpose is just to wrap created
 * {@link PreparedStatement} object and to log its command before execution.
 */
public class PreparedStatementLogWrapper extends JDBC4PreparedStatement {

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(PreparedStatementLogWrapper.class);

    /**
     * Constructor for the class.
     * 
     * @param stmnt - statement to wrap.
     * @throws SQLException - in case of error.
     */
    public PreparedStatementLogWrapper(JDBC4PreparedStatement stmnt) throws SQLException {
        super(stmnt.conn, stmnt.sql);
    }

    @Override
    public boolean execute() throws SQLException {
        if (sLog.isDebugEnabled()) {
            sLog.debug("Executing :" + constructQuery());
        }
        return super.execute();
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        if (sLog.isDebugEnabled()) {
            sLog.debug("Executing query:" + constructQuery());
        }
        return super.executeQuery();
    }

    /**
     * Construct actual query string, expanding parameters.
     * 
     * @return Constructed query.
     */
    private String constructQuery() {
        String query = this.sql;
        if (query.contains("?")) {
            for (Object param : batch) {
                query = query.replaceFirst("\\?", param.toString());
            }
        }
        return query;
    }
}
