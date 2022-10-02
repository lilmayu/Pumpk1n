package dev.mayuna.pumpk1n;

import com.zaxxer.hikari.HikariConfig;
import dev.mayuna.pumpk1n.impl.BufferedFolderStorageHandler;
import dev.mayuna.pumpk1n.impl.FolderStorageHandler;
import dev.mayuna.pumpk1n.impl.SQLStorageHandler;
import dev.mayuna.pumpk1n.impl.SQLiteStorageHandler;
import dev.mayuna.pumpk1n.objects.DataHolder;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class Pumpk1nTests {

    @Test
    public void testFolderStorageHandler() {
        Pumpk1n pumpk1n = new Pumpk1n(new FolderStorageHandler("./data/"));
        pumpk1n.prepareStorage();

        UUID uuid = UUID.randomUUID();

        DataHolder dataHolder = pumpk1n.getOrCreateDataHolder(uuid);
        AnotherTestData anotherTestData = dataHolder.getOrCreateDataElement(AnotherTestData.class);

        assertEquals(70, anotherTestData.someNumber);

        int newRandomNumber = new Random().nextInt();
        anotherTestData.someNumber = newRandomNumber;

        assertNotNull(anotherTestData.getDataHolderParent());
        anotherTestData.getDataHolderParent().save();

        // New instance ->
        pumpk1n = new Pumpk1n(new FolderStorageHandler("./data/"));
        pumpk1n.prepareStorage();

        dataHolder = pumpk1n.getOrCreateDataHolder(uuid);
        TestData testData = dataHolder.getOrCreateDataElement(TestData.class);

        assertEquals(newRandomNumber, testData.someNumber);

        dataHolder.delete();
    }

    @Test
    public void testBufferedFolderStorageHandler() {
        Pumpk1n pumpk1n = new Pumpk1n(new BufferedFolderStorageHandler("./data/", 3));
        pumpk1n.prepareStorage();

        UUID uuid = UUID.randomUUID();

        DataHolder dataHolder = pumpk1n.getOrCreateDataHolder(uuid);
        TestData testData = dataHolder.getOrCreateDataElement(TestData.class);

        assertEquals(69, testData.someNumber);

        int newRandomNumber = new Random().nextInt();
        testData.someNumber = newRandomNumber;

        assertNotNull(testData.getDataHolderParent());
        testData.getDataHolderParent().save();

        // New instance ->
        pumpk1n = new Pumpk1n(new BufferedFolderStorageHandler("./data/", 3));
        pumpk1n.prepareStorage();

        dataHolder = pumpk1n.getOrCreateDataHolder(uuid);
        testData = dataHolder.getOrCreateDataElement(TestData.class);

        assertEquals(newRandomNumber, testData.someNumber);

        dataHolder.delete();
    }


    @Test
    public void testSQLiteStorageHandler() {
        Pumpk1n pumpk1n = new Pumpk1n(new SQLiteStorageHandler(SQLiteStorageHandler.Settings.Builder.create()
                                                                                                    .setCustomJDBCUrl("jdbc:sqlite:./test/database.db")
                                                                                                    .build()));
        pumpk1n.prepareStorage();

        UUID uuid = UUID.randomUUID();

        DataHolder dataHolder = pumpk1n.getOrCreateDataHolder(uuid);
        TestData testData = dataHolder.getOrCreateDataElement(TestData.class);

        assertEquals(69, testData.someNumber);

        int newRandomNumber = new Random().nextInt();
        testData.someNumber = newRandomNumber;

        assertNotNull(testData.getDataHolderParent());
        testData.getDataHolderParent().save();

        // New instance ->
        pumpk1n = new Pumpk1n(new SQLiteStorageHandler(SQLiteStorageHandler.Settings.Builder.create()
                                                                                            .setCustomJDBCUrl("jdbc:sqlite:./test/database.db")
                                                                                            .build()));
        pumpk1n.prepareStorage();

        dataHolder = pumpk1n.getOrCreateDataHolder(uuid);
        testData = dataHolder.getOrCreateDataElement(TestData.class);

        assertEquals(newRandomNumber, testData.someNumber);

        dataHolder.delete();

        assertNull(pumpk1n.getDataHolder(dataHolder.getUuid()));

        //new File(((SQLiteStorageHandler) pumpk1n.getStorageHandler()).getSettings().getFileName() + ".db").delete();
    }

    //@Test
    public void testSQLStorageHandler() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + "ip" + ":" + 3306 + "/" + "database" + "?characterEncoding=UTF-8&autoReconnect=true&useSSL=" + true);
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setUsername("username");
        hikariConfig.setPassword("password");
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.setConnectionTimeout(30000);

        Pumpk1n pumpk1n = new Pumpk1n(new SQLStorageHandler(hikariConfig, "pumpkin"));
        pumpk1n.prepareStorage();

        UUID uuid = UUID.randomUUID();

        DataHolder dataHolder = pumpk1n.getOrLoadDataHolder(uuid);
        TestData testData = dataHolder.getOrCreateDataElement(TestData.class);

        assertEquals(69, testData.someNumber);

        int newRandomNumber = new Random().nextInt();
        testData.someNumber = newRandomNumber;

        dataHolder.save();

        // New instance ->
        pumpk1n = new Pumpk1n(new SQLStorageHandler(hikariConfig, "pumpkin"));
        pumpk1n.prepareStorage();

        dataHolder = pumpk1n.getOrLoadDataHolder(uuid);
        testData = dataHolder.getOrCreateDataElement(TestData.class);

        assertEquals(newRandomNumber, testData.someNumber);

        dataHolder.delete();

        assertNull(pumpk1n.getDataHolder(dataHolder.getUuid()));

        try {
            Connection connection = ((SQLStorageHandler) pumpk1n.getStorageHandler()).getPoolManager().getConnection();

            connection.prepareStatement("DROP TABLE pumpkin").executeUpdate();

            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
