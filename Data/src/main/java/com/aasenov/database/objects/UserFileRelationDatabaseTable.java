package com.aasenov.database.objects;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.aasenov.database.WhereClauseManager;
import com.aasenov.database.WhereClauseParameter;
import com.aasenov.database.provider.DatabaseProvider;

/**
 * Representation of database table, containing {@link UserFileRelationItem} objects as rows.
 */
public class UserFileRelationDatabaseTable extends DatabaseTable<UserFileRelationItem> {

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(UserFileRelationDatabaseTable.class);

    /**
     * Create instance of table with given name.
     * 
     * @param tableName - name of table to create.
     */
    public UserFileRelationDatabaseTable(String tableName) {
        super(tableName, new UserFileRelationItem(null, null));
    }

    /**
     * Retrieve files for given user.
     * 
     * @param userID - ID of user to retrieve files for.
     * @param startIndex - index from where to start selecting.
     * @param count - number of fileIDs to return.
     * @return List of fileIDs, that given user has access to.
     */
    public List<String> getFilesForUser(String userID, int startIndex, int count) {
        List<String> res = new ArrayList<String>();
        try {
            // select from databse
            WhereClauseManager whereClauseManager = new WhereClauseManager();
            whereClauseManager.getAndCollection().add(
                    new WhereClauseParameter(UserFileRelationItem.COLUMN_USER_ID, userID));

            List<UserFileRelationItem> lst = DatabaseProvider.getDefaultManager().<UserFileRelationItem> select(
                    getTableName(), whereClauseManager.toString(), startIndex, count, null, null);
            if (lst != null && !lst.isEmpty()) {
                for (UserFileRelationItem rel : lst) {
                    res.add(rel.getFileID());
                }
            }
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
        }
        return res;
    }

    /**
     * Retrieve number of files for given user.
     * 
     * @param userID - ID of user to retrieve files for.
     * @return Number of files that given user has.
     */
    public long getTotalFilesForUser(String userID) {
        long res = 0;
        try {
            // select from databse
            WhereClauseManager whereClauseManager = new WhereClauseManager();
            whereClauseManager.getAndCollection().add(
                    new WhereClauseParameter(UserFileRelationItem.COLUMN_USER_ID, userID));

            res = DatabaseProvider.getDefaultManager().getNumRows(getTableName(), whereClauseManager.toString());
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
        }
        return res;
    }

    /**
     * Retrieve users that has access to given file.
     * 
     * @param fileID - ID of user to retrieve files for.
     * @param startIndex - index from where to start selecting.
     * @param count - number of userIDs to return.
     * @return List of userIDs, that has access to passed file.
     */
    public List<String> getUsersForFile(String fileID, int startIndex, int count) {
        List<String> res = new ArrayList<String>();
        try {
            // select from databse
            WhereClauseManager whereClauseManager = new WhereClauseManager();
            whereClauseManager.getAndCollection().add(
                    new WhereClauseParameter(UserFileRelationItem.COLUMN_FILE_ID, fileID));

            List<UserFileRelationItem> lst = DatabaseProvider.getDefaultManager().<UserFileRelationItem> select(
                    getTableName(), whereClauseManager.toString(), startIndex, count, null, null);
            if (lst != null && !lst.isEmpty()) {
                for (UserFileRelationItem rel : lst) {
                    res.add(rel.getUserID());
                }
            }

        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
        }
        return res;
    }

    /**
     * Retrieve number of files for given user.
     * 
     * @param fileID - ID of file to retrieve users for.
     * @return Number of files that given user has.
     */
    public long getTotalUsersForFile(String fileID) {
        long res = 0;
        try {
            // select from databse
            WhereClauseManager whereClauseManager = new WhereClauseManager();
            whereClauseManager.getAndCollection().add(
                    new WhereClauseParameter(UserFileRelationItem.COLUMN_FILE_ID, fileID));

            res = DatabaseProvider.getDefaultManager().getNumRows(getTableName(), whereClauseManager.toString());
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
        }
        return res;
    }

    /**
     * Retrieve rating for given file, based on ratings, assigned from all users.
     * 
     * @param fileID - ID of file to retrieve rating for.
     * @return File rating.
     */
    public double getRatingForFile(String fileID) {
        double result = 0;
        try {
            // select from databse
            WhereClauseManager whereClauseManager = new WhereClauseManager();
            whereClauseManager.getAndCollection().add(
                    new WhereClauseParameter(UserFileRelationItem.COLUMN_FILE_ID, fileID));
            whereClauseManager.getAndCollection().add(
                    new WhereClauseParameter(UserFileRelationItem.COLUMN_RATING, "0", ">"));

            result = DatabaseProvider.getDefaultManager().average(getTableName(), UserFileRelationItem.COLUMN_RATING,
                    whereClauseManager.toString());
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
        }

        return result;
    }
}
