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
public class FilesTableTest {

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
        DatabaseTable<FileItem> filesTable = new DatabaseTable<FileItem>(FileItem.DEFAULT_TABLE_NAME,
                new FileItem(null));
        DatabaseTable<FileItem> filesTable1 = new DatabaseTable<FileItem>(FileItem.DEFAULT_TABLE_NAME, new FileItem(
                null));
        assertEquals("Problem creating table", filesTable.size(), filesTable1.size());

        FileItem file1 = new FileItem("name1", "hash1", "location1", "speechLocation1", "speechBySlidesLocation1",
                "parsedLocation1");
        FileItem file2 = new FileItem("name2", "hash2", "location2", "speechLocation2", "speechBySlidesLocation2",
                "parsedLocation2");
        filesTable.add(file1);
        assertEquals("Problem adding file to table", 1, filesTable1.size());
        FileItem storedFile = filesTable.get(file1.getID());
        compareFiles(file1, storedFile);

        filesTable.remove(file1.getID());
        assertEquals("Problem removing file from table", 0, filesTable1.size());

        filesTable.addAll(file1, file2);
        assertEquals("Problem adding multiple files to table", 2, filesTable1.size());
        compareFiles(file1, filesTable1.get(file1.getID()));
        compareFiles(file2, filesTable1.get(file2.getID()));

        List<FileItem> files = filesTable1.getAll(Arrays.asList(file1.getID(), file2.getID()));
        assertEquals("Problem using getAll method", 2, files.size());
        for (FileItem file : files) {
            if (file.getID().equals(file1.getID())) {
                compareFiles(file1, file);
            } else {
                compareFiles(file2, file);
            }
        }

        FileItem file3 = new FileItem("name3", "hash3", "location3", "speechLocation3", "speechBySlidesLocation3",
                "parsedLocation3");
        FileItem file4 = new FileItem("name4", "hash4", "location4", "speechLocation4", "speechBySlidesLocation4",
                "parsedLocation4");
        FileItem file5 = new FileItem("name5", "hash5", "location5", "speechLocation5", "speechBySlidesLocation5",
                "parsedLocation5");
        filesTable.addAll(file3, file4, file5);
        assertEquals("Problem using getAll method", 5, filesTable1.size());
        assertEquals("Problem using getPage method", 5, filesTable1.getPage(0, 10).size());
        assertEquals("Problem using getPage method", 0, filesTable1.getPage(5, 10).size());
        compareFiles(file1, filesTable1.getPage(0, 1, SortOrder.ASCENDING, new String[] { "Name" }).get(0));
        compareFiles(file2, filesTable1.getPage(1, 2, SortOrder.ASCENDING, new String[] { "Name" }).get(0));
        compareFiles(file3, filesTable1.getPage(2, 3, SortOrder.ASCENDING, new String[] { "Name" }).get(0));
        compareFiles(file4, filesTable1.getPage(3, 4, SortOrder.ASCENDING, new String[] { "Name" }).get(0));
        compareFiles(file5, filesTable1.getPage(4, 5, SortOrder.ASCENDING, new String[] { "Name" }).get(0));

        // test update
        FileItem forUpdate = filesTable.get(file1.getID());
        forUpdate.setName("differentName");
        forUpdate.setLocation("differentLocation");
        filesTable.add(forUpdate);
        compareFiles(forUpdate, filesTable1.get(forUpdate.getID()));
    }

    private void compareFiles(FileItem file1, FileItem file2) {
        assertEquals("Files Hash is different", file1.getHash(), file2.getHash());
        assertEquals("Files ID is different", file1.getID(), file2.getID());
        assertEquals("Files Location is different", file1.getLocation(), file2.getLocation());
        assertEquals("Files Name is different", file1.getName(), file2.getName());
        assertEquals("Files ParsedLocation is different", file1.getParsedLocation(), file2.getParsedLocation());
        assertEquals("Files SpeechBySlidesLocation is different", file1.getSpeechBySlidesLocation(),
                file2.getSpeechBySlidesLocation());
        assertEquals("Files SpeechLocation is different", file1.getSpeechLocation(), file2.getSpeechLocation());
    }

}
