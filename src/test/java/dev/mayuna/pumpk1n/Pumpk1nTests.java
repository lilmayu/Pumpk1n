package dev.mayuna.pumpk1n;

import dev.mayuna.pumpk1n.api.Migratable;
import dev.mayuna.pumpk1n.api.StorageHandler;
import dev.mayuna.pumpk1n.impl.BufferedFolderStorageHandler;
import dev.mayuna.pumpk1n.impl.FolderStorageHandler;
import dev.mayuna.pumpk1n.impl.SQLiteStorageHandler;
import dev.mayuna.pumpk1n.objects.DataHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class Pumpk1nTests {

    public static String FOLDER_STORAGE_PATH = "./data/";
    public static String BUFFERED_FOLDER_STORAGE_PATH = "./buffered_data/";
    public static String SQLITE_STORAGE_FOLDER_PATH = "./sqlite_data/";
    public static String SQLITE_STORAGE_PATH = SQLITE_STORAGE_FOLDER_PATH + "database.db";

    public static List<Pumpk1n> pumpk1ns = new LinkedList<>();

    private static List<StorageHandler> getStorageHandlers() {
        return List.of(
                new FolderStorageHandler(FOLDER_STORAGE_PATH),
                new BufferedFolderStorageHandler(BUFFERED_FOLDER_STORAGE_PATH, 10),
                new SQLiteStorageHandler(SQLiteStorageHandler.Settings.Builder.create()
                                                                              .setCustomJDBCUrl("jdbc:sqlite:" + SQLITE_STORAGE_PATH)
                                                                              .build())
        );
    }

    private static void deleteAllIn(String path) {
        File folder = new File(path);

        if (!folder.exists()) {
            return;
        }

        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }

        folder.delete();
    }

    @BeforeEach
    public void preparePumpk1ns() {
        new File("./sqlite_data/").mkdirs();

        getStorageHandlers().forEach(storageHandler -> {
            Pumpk1n pumpk1n = new Pumpk1n(storageHandler);
            pumpk1n.prepareStorage();
            pumpk1ns.add(pumpk1n);
        });
    }

    @AfterEach
    public void destroyPumpk1ns() {
        pumpk1ns.forEach(pumpk1n -> {
            if (pumpk1n.getStorageHandler() instanceof Migratable) {
                ((Migratable) pumpk1n.getStorageHandler()).getAllHolderUUIDs().forEach(pumpk1n::deleteDataHolder);
            }
        });

        deleteAllIn(FOLDER_STORAGE_PATH);
        deleteAllIn(BUFFERED_FOLDER_STORAGE_PATH);
        deleteAllIn(SQLITE_STORAGE_FOLDER_PATH);
    }

    @Test
    public void testDataPersistence() {
        pumpk1ns.forEach(pumpk1n -> {
            UUID uuid = UUID.randomUUID();

            assertNull(pumpk1n.getDataHolder(uuid));
            assertNull(pumpk1n.getOrLoadDataHolder(uuid));

            DataHolder dataHolder = pumpk1n.getOrCreateDataHolder(uuid);

            assertNotNull(dataHolder);

            assertNull(dataHolder.getDataElement(AnotherTestData.class));

            AnotherTestData anotherTestData = dataHolder.getOrCreateDataElement(AnotherTestData.class);

            assertNotNull(anotherTestData);
            assertEquals(new AnotherTestData().someNumber, anotherTestData.someNumber); // Sanity check

            int randomNumber = new Random().nextInt();

            anotherTestData.someNumber = randomNumber;
            anotherTestData.getDataHolderParent().save();

            Pumpk1n anotherPumpk1n = new Pumpk1n(pumpk1n.getStorageHandler());
            anotherPumpk1n.prepareStorage();

            assertNull(anotherPumpk1n.getDataHolder(uuid));

            dataHolder = anotherPumpk1n.getOrLoadDataHolder(uuid);

            assertNotNull(dataHolder);

            TestData testData = dataHolder.getDataElement(TestData.class); // Testing BackwardsCompatible annotation

            assertNotNull(testData);

            assertEquals(randomNumber, testData.someNumber);

            assertTrue(dataHolder.delete());
        });
    }

    @Test
    public void testMigration() {
        pumpk1ns.forEach(pumpk1n -> {
            UUID uuid = UUID.randomUUID();
            UUID uuid2 = UUID.randomUUID();
            int randomNumber = new Random().nextInt();

            DataHolder dataHolder = pumpk1n.getOrCreateDataHolder(uuid);

            TestData testData = dataHolder.getOrCreateDataElement(TestData.class);
            testData.someNumber = randomNumber;
            dataHolder.save();

            dataHolder = pumpk1n.getOrCreateDataHolder(uuid2);

            AnotherTestData anotherTestData = dataHolder.getOrCreateDataElement(AnotherTestData.class);
            anotherTestData.someNumber = randomNumber;
            dataHolder.save();

            pumpk1n.unloadDataHolder(uuid2);

            getStorageHandlers().forEach(storageHandler -> {
                pumpk1n.migrateTo(storageHandler);

                assertEquals(pumpk1n.getStorageHandler().getClass(), storageHandler.getClass());

                assertNotNull(pumpk1n.getOrLoadDataHolder(uuid));
                assertNotNull(pumpk1n.getOrLoadDataHolder(uuid2));
            });
        });
    }
}
