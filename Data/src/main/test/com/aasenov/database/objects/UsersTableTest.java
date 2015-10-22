package com.aasenov.database.objects;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.swing.SortOrder;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aasenov.database.provider.DatabaseProvider;
import com.aasenov.helper.ConfigHelper;
import com.aasenov.helper.ConfigProperty;
import com.aasenov.helper.PathHelper;

/**
 * Testing {@link DatabaseTable} functionalities.
 */
public class UsersTableTest {

    private static Path sDataFolder;

    @BeforeClass
    public static void beforeClass() {
        // configure logger
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);

        // change path helper static field in order to set jar containing folder to temproray directory that will be
        // deleted after tests execution.
        try {
            sDataFolder = Files.createTempDirectory(null);

            // change field using reflection
            Field jarContainingFolder = PathHelper.class.getDeclaredField("sJarContainingFolder");
            jarContainingFolder.setAccessible(true);
            jarContainingFolder.set(PathHelper.class, sDataFolder.toFile().getAbsolutePath());

            // verify everything work correctly
            assertEquals(sDataFolder.toFile().getAbsolutePath(), PathHelper.getJarContainingFolder());

            // change config helper value if it's not correct
            ConfigHelper.getInstance().setConfigPropertyValue(ConfigProperty.StorageDir,
                    sDataFolder.toFile().getAbsolutePath());
            assertEquals(sDataFolder.toFile().getAbsolutePath(),
                    ConfigHelper.getInstance().getConfigPropertyValue(ConfigProperty.StorageDir));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @AfterClass
    public static void afterClass() {
        DatabaseProvider.destroyManagers();
        BasicConfigurator.resetConfiguration();
        // cleanup
        if (sDataFolder != null && sDataFolder.toFile().exists()) {
            try {
                FileUtils.deleteDirectory(sDataFolder.toFile());
            } catch (IOException e) {
                e.printStackTrace();
                Assert.fail();
            }
        }
    }

    @Test
    public void testOperations() {
        DatabaseTable<UserItem> usersTable = new DatabaseTable<UserItem>(UserItem.DEFAULT_TABLE_NAME,
                new UserItem(null));
        DatabaseTable<UserItem> usersTable1 = new DatabaseTable<UserItem>(UserItem.DEFAULT_TABLE_NAME, new UserItem(
                null));
        assertEquals("Problem creating table", usersTable.size(), usersTable1.size());

        UserItem user1 = new UserItem("test1", "testPass1", "testMail1", new SpeechSettings());
        UserItem user2 = new UserItem("test2", "testPass2", "testMail2", new SpeechSettings());
        usersTable.add(user1);
        assertEquals("Problem adding user to table", 1, usersTable1.size());
        UserItem storedUser = usersTable.get(user1.getID());
        compareUsers(user1, storedUser);

        usersTable.remove(user1.getID());
        assertEquals("Problem removing user from table", 0, usersTable1.size());

        usersTable.addAll(user1, user2);
        assertEquals("Problem adding multiple users to table", 2, usersTable1.size());
        compareUsers(user1, usersTable1.get(user1.getID()));
        compareUsers(user2, usersTable1.get(user2.getID()));

        List<UserItem> users = usersTable1.getAll(Arrays.asList(user1.getID(), user2.getID()));
        assertEquals("Problem using getAll method", 2, users.size());
        for (UserItem user : users) {
            if (user.getID().equals(user1.getID())) {
                compareUsers(user1, user);
            } else {
                compareUsers(user2, user);
            }
        }

        UserItem user3 = new UserItem("test3", "testPass3", "testMail3", new SpeechSettings());
        UserItem user4 = new UserItem("test4", "testPass4", "testMail4", new SpeechSettings());
        UserItem user5 = new UserItem("test5", "testPass5", "testMail5", new SpeechSettings());
        usersTable.addAll(user3, user4, user5);
        assertEquals("Problem using getAll method", 5, usersTable1.size());
        assertEquals("Problem using getPage method", 5, usersTable1.getPage(0, 10).size());
        assertEquals("Problem using getPage method", 0, usersTable1.getPage(5, 10).size());
        compareUsers(user1, usersTable1.getPage(0, 1, SortOrder.ASCENDING, new String[] { "Name" }).get(0));
        compareUsers(user2, usersTable1.getPage(1, 2, SortOrder.ASCENDING, new String[] { "Name" }).get(0));
        compareUsers(user3, usersTable1.getPage(2, 3, SortOrder.ASCENDING, new String[] { "Name" }).get(0));
        compareUsers(user4, usersTable1.getPage(3, 4, SortOrder.ASCENDING, new String[] { "Name" }).get(0));
        compareUsers(user5, usersTable1.getPage(4, 5, SortOrder.ASCENDING, new String[] { "Name" }).get(0));

        // test update
        UserItem forUpdate = usersTable.get(user1.getID());
        forUpdate.setName("differentName");
        forUpdate.setPassword("differentPass");
        usersTable.add(forUpdate);
        compareUsers(forUpdate, usersTable1.get(forUpdate.getID()));
    }

    private void compareUsers(UserItem user1, UserItem user2) {
        assertEquals("Users Email is different", user1.getEmail(), user2.getEmail());
        assertEquals("Users Password is different", user1.getPassword(), user2.getPassword());
        assertEquals("Users Name is different", user1.getName(), user2.getName());
        assertEquals("Users ID is different", user1.getID(), user2.getID());
        // compare speech settings
        if (user1.getSpeechSettings() == null || user2.getSpeechSettings() == null) {
            assertEquals("Users user SpeechSettings is different", user1.getSpeechSettings(), user2.getSpeechSettings());
        } else {
            SpeechSettings settings1 = user1.getSpeechSettings();
            SpeechSettings settings2 = user2.getSpeechSettings();
            assertEquals("User settings Amplitude is different", settings1.getAmplitude(), settings2.getAmplitude());
            assertEquals("User settings Capitals is different", settings1.getCapitals(), settings2.getCapitals());
            assertEquals("User settings Encoding is different", settings1.getEncoding(), settings2.getEncoding());
            assertEquals("User settings Language is different", settings1.getLanguage(), settings2.getLanguage());
            assertEquals("User settings LineLength is different", settings1.getLineLength(), settings2.getLineLength());
            assertEquals("User settings Pitch is different", settings1.getPitch(), settings2.getPitch());
            assertEquals("User settings Speed is different", settings1.getSpeed(), settings2.getSpeed());
            assertEquals("User settings WordGap is different", settings1.getWordGap(), settings2.getWordGap());
        }

    }

}
