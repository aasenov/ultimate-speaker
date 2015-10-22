package com.aasenov.database.objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

import com.aasenov.database.err.NotInRangeException;
import com.aasenov.database.provider.DatabaseProvider;
import com.aasenov.helper.ConfigHelper;
import com.aasenov.helper.ConfigProperty;
import com.aasenov.helper.PathHelper;

/**
 * Testing {@link DatabaseTable} functionalities.
 */
public class UserFileRelationDatabaseTableTest {

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
    public void testSimpleOperations() throws NotInRangeException {
        DatabaseProvider.getDefaultManager().deleteAllTableContents();
        UserFileRelationDatabaseTable userFileRelTable = new UserFileRelationDatabaseTable(
                UserFileRelationItem.DEFAULT_TABLE_NAME);
        UserFileRelationDatabaseTable userFileRelTable1 = new UserFileRelationDatabaseTable(
                UserFileRelationItem.DEFAULT_TABLE_NAME);
        assertEquals("Problem creating table", userFileRelTable.size(), userFileRelTable1.size());

        UserFileRelationItem userFileRel1 = new UserFileRelationItem("user1", "file1",
                UserFileRelationItem.RATING_MAX_VALUE);
        UserFileRelationItem userFileRel2 = new UserFileRelationItem("user2", "file2",
                UserFileRelationItem.RATING_MAX_VALUE);
        userFileRelTable.add(userFileRel1);
        assertEquals("Problem adding userFileRel to table", 1, userFileRelTable1.size());
        UserFileRelationItem storedUser = userFileRelTable.get(userFileRel1.getID());
        compareUserFileRelations(userFileRel1, storedUser);

        userFileRelTable.remove(userFileRel1.getID());
        assertEquals("Problem removing userFileRel from table", 0, userFileRelTable1.size());

        userFileRelTable.addAll(userFileRel1, userFileRel2);
        assertEquals("Problem adding multiple userFileRel to table", 2, userFileRelTable1.size());
        compareUserFileRelations(userFileRel1, userFileRelTable1.get(userFileRel1.getID()));
        compareUserFileRelations(userFileRel2, userFileRelTable1.get(userFileRel2.getID()));

        List<UserFileRelationItem> users = userFileRelTable1.getAll(Arrays.asList(userFileRel1.getID(),
                userFileRel2.getID()));
        assertEquals("Problem using getAll method", 2, users.size());
        for (UserFileRelationItem user : users) {
            if (user.getID().equals(userFileRel1.getID())) {
                compareUserFileRelations(userFileRel1, user);
            } else {
                compareUserFileRelations(userFileRel2, user);
            }
        }

        UserFileRelationItem userFileRel3 = new UserFileRelationItem("user3", "file3",
                UserFileRelationItem.RATING_MAX_VALUE);
        UserFileRelationItem userFileRel4 = new UserFileRelationItem("user4", "file4",
                UserFileRelationItem.RATING_MAX_VALUE);
        UserFileRelationItem userFileRel5 = new UserFileRelationItem("user5", "file5",
                UserFileRelationItem.RATING_MAX_VALUE);
        userFileRelTable.addAll(userFileRel3, userFileRel4, userFileRel5);
        assertEquals("Problem using getAll method", 5, userFileRelTable1.size());
        assertEquals("Problem using getPage method", 5, userFileRelTable1.getPage(0, 10).size());
        assertEquals("Problem using getPage method", 0, userFileRelTable1.getPage(5, 10).size());
        compareUserFileRelations(
                userFileRel1,
                userFileRelTable1.getPage(0, 1, SortOrder.ASCENDING,
                        new String[] { UserFileRelationItem.COLUMN_FILE_ID }).get(0));
        compareUserFileRelations(
                userFileRel2,
                userFileRelTable1.getPage(1, 2, SortOrder.ASCENDING,
                        new String[] { UserFileRelationItem.COLUMN_FILE_ID }).get(0));
        compareUserFileRelations(
                userFileRel3,
                userFileRelTable1.getPage(2, 3, SortOrder.ASCENDING,
                        new String[] { UserFileRelationItem.COLUMN_FILE_ID }).get(0));
        compareUserFileRelations(
                userFileRel4,
                userFileRelTable1.getPage(3, 4, SortOrder.ASCENDING,
                        new String[] { UserFileRelationItem.COLUMN_FILE_ID }).get(0));
        compareUserFileRelations(
                userFileRel5,
                userFileRelTable1.getPage(4, 5, SortOrder.ASCENDING,
                        new String[] { UserFileRelationItem.COLUMN_FILE_ID }).get(0));

        // test update
        UserFileRelationItem forUpdate = userFileRelTable.get(userFileRel1.getID());
        forUpdate.setRating(UserFileRelationItem.RATING_MIN_VALUE);
        userFileRelTable.add(forUpdate);
        compareUserFileRelations(forUpdate, userFileRelTable1.get(forUpdate.getID()));
    }

    @Test
    public void testCustomOperations() throws NotInRangeException {
        DatabaseProvider.getDefaultManager().deleteAllTableContents();
        UserFileRelationDatabaseTable userFileRelTable = new UserFileRelationDatabaseTable(
                UserFileRelationItem.DEFAULT_TABLE_NAME);
        UserFileRelationDatabaseTable userFileRelTable1 = new UserFileRelationDatabaseTable(
                UserFileRelationItem.DEFAULT_TABLE_NAME);
        assertEquals("Problem creating table", userFileRelTable.size(), userFileRelTable1.size());

        UserFileRelationItem userFileRel1 = new UserFileRelationItem("user1", "file1", 5);
        UserFileRelationItem userFileRel2 = new UserFileRelationItem("user1", "file2", 10);
        UserFileRelationItem userFileRel3 = new UserFileRelationItem("user2", "file1", 10);
        UserFileRelationItem userFileRel4 = new UserFileRelationItem("user3", "file1", 0);
        UserFileRelationItem userFileRel5 = new UserFileRelationItem("user3", "file2", 10);
        UserFileRelationItem userFileRel6 = new UserFileRelationItem("user3", "file3", 0);

        userFileRelTable.addAll(userFileRel1, userFileRel2, userFileRel3, userFileRel4, userFileRel5, userFileRel6);
        assertEquals("Problem adding userFileRel to table", 6, userFileRelTable1.size());

        List<String> filesForUser = userFileRelTable1.getFilesForUser("user1", 0, 10);
        assertEquals("Problem retrieving files for user1", 2, filesForUser.size());
        assertTrue("Problem retrieving files for user1",
                filesForUser.contains("file1") && filesForUser.contains("file2"));
        filesForUser = userFileRelTable1.getFilesForUser("user2", 0, 10);
        assertEquals("Problem retrieving files for user2", 1, filesForUser.size());
        assertTrue("Problem retrieving files for user2", filesForUser.contains("file1"));
        filesForUser = userFileRelTable1.getFilesForUser("user3", 0, 10);
        assertEquals("Problem retrieving files for user3", 3, filesForUser.size());
        assertTrue("Problem retrieving files for user3",
                filesForUser.contains("file1") && filesForUser.contains("file2") && filesForUser.contains("file3"));

        assertEquals("Problem retrieving total files for user1", 2, userFileRelTable1.getTotalFilesForUser("user1"));
        assertEquals("Problem retrieving total files for user2", 1, userFileRelTable1.getTotalFilesForUser("user2"));
        assertEquals("Problem retrieving total files for user3", 3, userFileRelTable1.getTotalFilesForUser("user3"));

        List<String> usersForFile = userFileRelTable1.getUsersForFile("file1", 0, 10);
        assertEquals("Problem retrieving files for file1", 3, usersForFile.size());
        assertTrue("Problem retrieving users for file1",
                usersForFile.contains("user1") && usersForFile.contains("user2") && usersForFile.contains("user3"));
        usersForFile = userFileRelTable1.getUsersForFile("file2", 0, 10);
        assertEquals("Problem retrieving users for file2", 2, usersForFile.size());
        assertTrue("Problem retrieving users for file2",
                usersForFile.contains("user1") && usersForFile.contains("user3"));
        usersForFile = userFileRelTable1.getUsersForFile("file3", 0, 10);
        assertEquals("Problem retrieving users for file3", 1, usersForFile.size());
        assertTrue("Problem retrieving users for file3", usersForFile.contains("user3"));

        assertEquals("Problem retrieving total users for file1", 3, userFileRelTable1.getTotalUsersForFile("file1"));
        assertEquals("Problem retrieving total users for file2", 2, userFileRelTable1.getTotalUsersForFile("file2"));
        assertEquals("Problem retrieving total users for file3", 1, userFileRelTable1.getTotalUsersForFile("file3"));

        assertEquals("Problem retrieving rating for file1", 7.5, userFileRelTable1.getRatingForFile("file1"), 0.1);
        assertEquals("Problem retrieving rating for file2", 10, userFileRelTable1.getRatingForFile("file2"), 0.1);
        assertEquals("Problem retrieving rating for file3", 0, userFileRelTable1.getRatingForFile("file3"), 0.1);

    }

    private void compareUserFileRelations(UserFileRelationItem userFileRel1, UserFileRelationItem userFileRel2) {
        assertEquals("Users FileID is different", userFileRel1.getFileID(), userFileRel2.getFileID());
        assertEquals("Users UserID is different", userFileRel1.getUserID(), userFileRel2.getUserID());
        assertEquals("Users Rating is different", userFileRel1.getRating(), userFileRel2.getRating(), 0.1);
        assertEquals("Users ID is different", userFileRel1.getID(), userFileRel2.getID());
    }

}
