package com.aasenov.database.objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

/**
 * Testing {@link FileItem} functionalities.
 */
public class FileItemTest {

    @Test
    public void testConstructors() {
        @SuppressWarnings("deprecation")
        FileItem fileItem = new FileItem();
        assertNotNull("Default constructor problem!", fileItem);

        String hash = UUID.randomUUID().toString();
        fileItem = new FileItem(hash);
        assertNotNull("Constructor with hash problem!", fileItem);
        assertEquals("Constructor with hash - hash not initialized", hash, fileItem.getHash());
        assertEquals("Constructor with hash - ID not initialized", hash, fileItem.getID());

        String name = UUID.randomUUID().toString();
        String location = UUID.randomUUID().toString();
        String speechLocation = UUID.randomUUID().toString();
        String speechBySlidesLocation = UUID.randomUUID().toString();
        String parsedLocation = UUID.randomUUID().toString();
        fileItem = new FileItem(name, hash, location, speechLocation, speechBySlidesLocation, parsedLocation);
        assertEquals("Common constructor - hash not initialized", hash, fileItem.getHash());
        assertEquals("Common constructor - id not initialized", hash, fileItem.getID());
        assertEquals("Common constructor - name not initialized", name, fileItem.getName());
        assertEquals("Common constructor - location not initialized", location, fileItem.getLocation());
        assertEquals("Common constructor - speechLocation not initialized", speechLocation,
                fileItem.getSpeechLocation());
        assertEquals("Common constructor - speechBySlidesLocation not initialized", speechBySlidesLocation,
                fileItem.getSpeechBySlidesLocation());
        assertEquals("Common constructor - parsedLocation not initialized", parsedLocation,
                fileItem.getParsedLocation());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testAccessors() {
        FileItem fileItem = new FileItem();
        assertNotNull("Default constructor problem!", fileItem);

        assertFalse("Unable to set forUpdate", fileItem.isForUpdate());
        fileItem.setForUpdate(true);
        assertTrue("Unable to set forUpdate", fileItem.isForUpdate());

        String randomString = UUID.randomUUID().toString();
        fileItem.setHash(randomString);
        assertEquals("Unable to set hash", randomString, fileItem.getHash());
        assertEquals("ID not automatically set when setting hash", randomString, fileItem.getID());

        randomString = UUID.randomUUID().toString();
        fileItem.setLocation(randomString);
        assertEquals("Unable to set location", randomString, fileItem.getLocation());

        randomString = UUID.randomUUID().toString();
        fileItem.setName(randomString);
        assertEquals("Unable to set Name", randomString, fileItem.getName());

        randomString = UUID.randomUUID().toString();
        fileItem.setParsedLocation(randomString);
        assertEquals("Unable to set ParsedLocation", randomString, fileItem.getParsedLocation());

        randomString = UUID.randomUUID().toString();
        fileItem.setRowID(randomString.hashCode());
        assertEquals("Unable to set RowID", randomString.hashCode(), fileItem.getRowID());

        randomString = UUID.randomUUID().toString();
        fileItem.setSpeechBySlidesLocation(randomString);
        assertEquals("Unable to set SpeechBySlidesLocation", randomString, fileItem.getSpeechBySlidesLocation());

        randomString = UUID.randomUUID().toString();
        fileItem.setSpeechLocation(randomString);
        assertEquals("Unable to set SpeechLocation", randomString, fileItem.getSpeechLocation());
    }

}
