package com.aasenov.database;

/**
 * This class stores value of one where clause statement.
 */
public class WhereClauseParameter {
    /**
     * Key of the statement.
     */
    private final String mKey;

    /**
     * Value of the statement.
     */
    private final String mValue;

    /**
     * Operator to use for comparison.
     */
    private final String mOperation;

    public WhereClauseParameter(String key, String val) {
        mKey = key;
        mValue = val;
        mOperation = "=";
    }

    public WhereClauseParameter(String key, String val, String operation) {
        mKey = key;
        mValue = val;
        mOperation = operation;
    }

    /**
     * Getter for the _key property.
     * 
     * @return the _key
     */
    public String getKey() {
        return mKey;
    }

    /**
     * Getter for the _value property.
     * 
     * @return the _value
     */
    public String getValue() {
        return DatabaseUtil.escapeString(mValue);
    }

    /**
     * Getter for the _operation property.
     * 
     * @return the _operation
     */
    public String getOperation() {
        return mOperation;
    }

    @Override
    public String toString() {
        return String.format("%s%s'%s'", getKey(), getOperation(), getValue());
    }
}
