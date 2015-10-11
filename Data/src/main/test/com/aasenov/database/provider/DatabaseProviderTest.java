package com.aasenov.database.provider;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNotSame;
import static junit.framework.TestCase.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aasenov.database.DatabaseManager;
import com.aasenov.database.manager.sqlite.SQLiteManager;
import com.aasenov.helper.PathHelper;

/**
 * Testing {@link DatabaseProvider} functionalities.
 */
public class DatabaseProviderTest {
    private static Path sConfFolder;

    @BeforeClass
    public static void beforeClass() {
        // configure logger
        BasicConfigurator.configure();

        // change path helper static field in order to set jar containing folder to temproray directory that will be
        // deleted after tests execution.
        try {
            sConfFolder = Files.createTempDirectory(null);

            // change field using reflection
            Field jarContainingFolder = PathHelper.class.getDeclaredField("sJarContainingFolder");
            jarContainingFolder.setAccessible(true);
            jarContainingFolder.set(PathHelper.class, sConfFolder.toFile().getAbsolutePath());

            // verify everything work correctly
            assertEquals(sConfFolder.toFile().getAbsolutePath(), PathHelper.getJarContainingFolder());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @AfterClass
    public static void afterClass() {
        // cleanup
        if (sConfFolder != null && sConfFolder.toFile().exists()) {
            try {
                FileUtils.deleteDirectory(sConfFolder.toFile());
            } catch (IOException e) {
                e.printStackTrace();
                Assert.fail();
            }
        }
    }

    @Test
    public void changeDatabaseType() {
        // check default value
        assertNotNull("Default database type is null!", DatabaseProvider.getDatabaseType());
        // check null assignment
        DatabaseProvider.setDatabaseType(null);
        assertNotNull("It is possible to assign null database type!", DatabaseProvider.getDatabaseType());

        // check with real values
        DatabaseProvider.setDatabaseType(DatabaseType.SQLite);
        assertEquals("Unable to change database type to SQLite!", DatabaseType.SQLite,
                DatabaseProvider.getDatabaseType());
    }

    @Test
    public void getDefaultManager() {
        // test retrieving default manager without initial setting database type
        assertNotNull("Default manager is null!", DatabaseProvider.getDefaultManager());

        // check retrieving exact manager
        DatabaseProvider.setDatabaseType(DatabaseType.SQLite);
        assertTrue("Unable to retrieve SQLiteManager!", DatabaseProvider.getDefaultManager() instanceof SQLiteManager);
    }

    @Test
    public void destroyManagers() {
        // here we should check that instances of two invocations of getDefaultManager should return same objects,
        // except if we didn't call destroyManagers
        assertEquals("Two invocations of getDefaultManager return different rezults!",
                DatabaseProvider.getDefaultManager(), DatabaseProvider.getDefaultManager());

        // check two invocations with destroy managers
        DatabaseManager manager1 = DatabaseProvider.getDefaultManager();
        DatabaseProvider.destroyManagers();
        DatabaseManager manager2 = DatabaseProvider.getDefaultManager();
        assertNotSame("Destroy managers doesn't clean static instances!", manager1, manager2);

    }
}
