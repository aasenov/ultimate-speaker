package com.aasenov.database;

import java.util.ArrayList;

/**
 * Use this class to collect where clauses that are connected with same operator (AND/OR).
 */
public class WhereClauseParameterCollection extends ArrayList<WhereClauseParameter> {

    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Operator used for joining the statements.
     */
    private String mJoinOperator;

    public WhereClauseParameterCollection(String join) {
        super();
        mJoinOperator = join;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (WhereClauseParameter pair : this) {
            sb.append(" ");
            sb.append(pair.toString());
            sb.append(mJoinOperator);
        }
        sb.delete(sb.length() - (mJoinOperator.length()), sb.length());// delete last operator
        return sb.toString().trim();
    }
}
