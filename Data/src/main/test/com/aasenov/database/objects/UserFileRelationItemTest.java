package com.aasenov.database.objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

import com.aasenov.database.err.NotInRangeException;

/**
 * Testing {@link UserFileRelationItem} functionalities.
 */
public class UserFileRelationItemTest {

    @Test
    public void testConstructors() throws NotInRangeException {
        @SuppressWarnings("deprecation")
        UserFileRelationItem userFileRelationItem = new UserFileRelationItem();
        assertNotNull("Default constructor problem!", userFileRelationItem);

        String userID = UUID.randomUUID().toString();
        String fileID = UUID.randomUUID().toString();
        userFileRelationItem = new UserFileRelationItem(userID, fileID);
        assertNotNull("Constructor with userID and fileID problem!", userFileRelationItem);
        assertEquals("Constructor with userID and fileID - userID not initialized", userID,
                userFileRelationItem.getUserID());
        assertEquals("Constructor with userID and fileID - fileID not initialized", fileID,
                userFileRelationItem.getFileID());
        assertTrue("Constructor with userID and fileID - ID not initialized correctly", userFileRelationItem.getID()
                .contains(fileID) && userFileRelationItem.getID().contains(userID));

        double raiting = UserFileRelationItem.RATING_MAX_VALUE;
        userFileRelationItem = new UserFileRelationItem(userID, fileID, raiting);
        assertNotNull("Common constructor problem!", userFileRelationItem);
        assertEquals("Common constructor - userID not initialized", userID, userFileRelationItem.getUserID());
        assertEquals("Common constructor - fileID not initialized", fileID, userFileRelationItem.getFileID());
        assertTrue("Common constructor - ID not initialized correctly", userFileRelationItem.getID().contains(fileID)
                && userFileRelationItem.getID().contains(userID));
        assertEquals("Common constructor - raiting not initialized", raiting, userFileRelationItem.getRating(), 0.1);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testAccessors() throws NotInRangeException {
        UserFileRelationItem userFileRelationItem = new UserFileRelationItem();
        assertNotNull("Default constructor problem!", userFileRelationItem);

        assertFalse("Unable to set forUpdate", userFileRelationItem.isForUpdate());
        userFileRelationItem.setForUpdate(true);
        assertTrue("Unable to set forUpdate", userFileRelationItem.isForUpdate());

        String randomString = UUID.randomUUID().toString();
        userFileRelationItem.setUserID(randomString);
        assertEquals("Unable to set userID", randomString, userFileRelationItem.getUserID());
        assertTrue("ID not automatically set when setting userID", userFileRelationItem.getID().contains(randomString));

        randomString = UUID.randomUUID().toString();
        userFileRelationItem.setFileID(randomString);
        assertEquals("Unable to set FileID", randomString, userFileRelationItem.getFileID());
        assertTrue("ID not automatically set when setting userID", userFileRelationItem.getID().contains(randomString));

        double raiting = UserFileRelationItem.RATING_MAX_VALUE;
        userFileRelationItem.setRating(raiting);
        assertEquals("Unable to set rating", raiting, userFileRelationItem.getRating(), 0.1);

        randomString = UUID.randomUUID().toString();
        userFileRelationItem.setRowID(randomString.hashCode());
        assertEquals("Unable to set RowID", randomString.hashCode(), userFileRelationItem.getRowID());
    }

}
