package com.aasenov.database.objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

/**
 * Testing {@link UserItem} functionalities.
 */
public class UserItemTest {

    @Test
    public void testConstructors() {
        @SuppressWarnings("deprecation")
        UserItem UserItem = new UserItem();
        assertNotNull("Default constructor problem!", UserItem);

        String email = UUID.randomUUID().toString();
        UserItem = new UserItem(email);
        assertNotNull("Constructor with email problem!", UserItem);
        assertEquals("Constructor with email - email not initialized", email, UserItem.getEmail());
        assertEquals("Constructor with email - ID not initialized", email, UserItem.getID());

        String name = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();
        UserItem = new UserItem(name, password, email);
        assertEquals("Common constructor - email not initialized", email, UserItem.getEmail());
        assertEquals("Common constructor - id not initialized", email, UserItem.getID());
        assertEquals("Common constructor - name not initialized", name, UserItem.getName());
        assertEquals("Common constructor - password not initialized", password, UserItem.getPassword());

        SpeechSettings speechSettings = new SpeechSettings();
        UserItem = new UserItem(name, password, email, speechSettings);
        assertEquals("Common constructor - email not initialized", email, UserItem.getEmail());
        assertEquals("Common constructor - id not initialized", email, UserItem.getID());
        assertEquals("Common constructor - name not initialized", name, UserItem.getName());
        assertEquals("Common constructor - password not initialized", password, UserItem.getPassword());
        assertEquals("Common constructor - speechSettings not initialized", speechSettings,
                UserItem.getSpeechSettings());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testAccessors() {
        UserItem UserItem = new UserItem();
        assertNotNull("Default constructor problem!", UserItem);

        assertFalse("Unable to set forUpdate", UserItem.isForUpdate());
        UserItem.setForUpdate(true);
        assertTrue("Unable to set forUpdate", UserItem.isForUpdate());

        String randomString = UUID.randomUUID().toString();
        UserItem.setEmail(randomString);
        assertEquals("Unable to set email", randomString, UserItem.getEmail());
        assertEquals("ID not automatically set when setting email", randomString, UserItem.getID());

        randomString = UUID.randomUUID().toString();
        UserItem.setPassword(randomString);
        assertEquals("Unable to set Password", randomString, UserItem.getPassword());

        randomString = UUID.randomUUID().toString();
        UserItem.setName(randomString);
        assertEquals("Unable to set Name", randomString, UserItem.getName());

        SpeechSettings settings = new SpeechSettings();
        UserItem.setSpeechSettings(settings);
        assertEquals("Unable to set SpeechSettings", settings, UserItem.getSpeechSettings());

        randomString = UUID.randomUUID().toString();
        UserItem.setRowID(randomString.hashCode());
        assertEquals("Unable to set RowID", randomString.hashCode(), UserItem.getRowID());
    }

}
