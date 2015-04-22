package com.aasenov.database;

/**
 * Manager that keep track of used Where statement conditions.
 */
public class WhereClauseManager {
    /**
     * Collection that keeps AND statements
     */
    private WhereClauseParameterCollection mAndCollection;

    /**
     * Collectino that keeps OR statements.
     */
    private WhereClauseParameterCollection mOrCollection;

    public WhereClauseManager() {
        mAndCollection = new WhereClauseParameterCollection("AND");
        mOrCollection = new WhereClauseParameterCollection("OR");
    }

    /**
     * Retrieve collection containing AND where clauses.
     * 
     * @return Collection for the AND statements.
     */
    public WhereClauseParameterCollection getAndCollection() {
        return mAndCollection;
    }

    /**
     * Retrieve collection containing OR where clauses.
     * 
     * @return Collection for the OR statements.
     */
    public WhereClauseParameterCollection getOrCollection() {
        return mOrCollection;
    }

    @Override
    public String toString() {
        String str = null;

        if (mAndCollection.size() > 0 && mOrCollection.size() > 0) {
            str = String.format("%s AND (%s)", mAndCollection.toString(), mOrCollection.toString());
        } else if (mAndCollection.size() > 0) {
            str = mAndCollection.toString();
        } else if (mOrCollection.size() > 0) {
            str = mOrCollection.toString();
        }
        if (str != null && !str.isEmpty()) {
            str = "WHERE " + str;
        }

        return str;
    }
}
