package dev.mayuna.pumpk1n.impl;

import com.google.gson.JsonParser;
import dev.mayuna.pumpk1n.api.StorageHandler;
import dev.mayuna.pumpk1n.objects.DataHolder;
import lombok.Getter;
import lombok.NonNull;

import java.sql.*;
import java.util.UUID;

/**
 * SQLite based storage
 */
public class SQLiteStorageHandler extends StorageHandler {

    private static final Object mutex = new Object();
    private final @Getter Settings settings;

    /**
     * Creates SQLite Storage Handler with default settings
     */
    public SQLiteStorageHandler() {
        super(SQLiteStorageHandler.class.getSimpleName());
        this.settings = new Settings.Builder().build();
    }

    /**
     * Creates SQLite Storage Handler with specified {@link Settings}
     */
    public SQLiteStorageHandler(Settings settings) {
        super(SQLiteStorageHandler.class.getSimpleName());
        this.settings = settings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareStorage() {
        try {
            connectToDatabase().close();
            createDatabase();
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveHolder(DataHolder dataHolder) {
        insertOrReplace(dataHolder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataHolder loadHolder(UUID uuid) {
        return loadByUUID(uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeHolder(UUID uuid) {
        return delete(uuid);
    }

    ////////////////////
    // SQLite methods //
    ////////////////////

    private Connection connectToDatabase() {
        synchronized (mutex) {
            try {
                String jdbcUrl = settings.customJDBCUrl;

                if (jdbcUrl == null) {
                    jdbcUrl = "jdbc:sqlite:" + settings.fileName;
                }

                return DriverManager.getConnection(jdbcUrl);
            } catch (Exception exception) {
                throw new RuntimeException(new SQLException("Could not create connection to sqlite database!", exception));
            }
        }
    }

    private void createDatabase() {
        synchronized (mutex) {
            try (Connection connection = connectToDatabase()) {
                try (Statement statement = connection.createStatement()) {
                    String sql = "CREATE TABLE IF NOT EXISTS " + settings.tableName + " (";
                    sql += "uuid VARCHAR(36) PRIMARY KEY NOT NULL,";
                    sql += "data JSON NOT NULL";
                    sql += ");";

                    statement.execute(sql);
                }
            } catch (SQLException exception) {
                throw new RuntimeException("Exception occurred while creating " + settings.tableName + " table in SQLite database!", exception);
            }
        }
    }

    private void insertOrReplace(@NonNull DataHolder dataHolder) {
        synchronized (mutex) {
            try (Connection connection = connectToDatabase()) {
                String sql = "REPLACE INTO " + settings.tableName + " (uuid, data) VALUES (?, ?);";

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, dataHolder.getUuid().toString());
                    statement.setString(2, dataHolder.getAsJsonObject().toString());

                    statement.executeUpdate();
                }
            } catch (SQLException exception) {
                throw new RuntimeException("Exception occurred while saving DataHolder with UUID " + dataHolder.getUuid() + " to SQLite database!", exception);
            }
        }
    }

    private DataHolder loadByUUID(UUID uuid) {
        synchronized (mutex) {
            try (Connection connection = connectToDatabase()) {
                String sql = "SELECT data FROM " + settings.tableName + " WHERE uuid = ?;";

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, uuid.toString());

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            return DataHolder.loadFromJsonObject(getPumpk1n(), JsonParser.parseString(resultSet.getString("data")).getAsJsonObject());
                        }
                    }
                }
            } catch (SQLException exception) {
                throw new RuntimeException("Exception occurred while loading DataHolder with UUID " + uuid + "  from SQLite database!", exception);
            }
        }

        return null;
    }

    private boolean delete(@NonNull UUID uuid) {
        synchronized (mutex) {
            try (Connection connection = connectToDatabase()) {
                String sql = "DELETE FROM " + settings.tableName + " WHERE uuid = ?;";

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, uuid.toString());

                    return statement.executeUpdate() > 0;
                }
            } catch (SQLException exception) {
                throw new RuntimeException("Exception occurred while deleting DataHolder with UUID " + uuid + " from SQLite database!", exception);
            }
        }
    }

    public static class Settings {

        private final @Getter String customJDBCUrl;
        private final @Getter String fileName;
        private final @Getter String tableName;

        /**
         * Creates {@link Settings} object. It's recommended that you use {@link Builder} to create it.
         *
         * @param customJDBCUrl Custom JDBC URL to use. If this argument is not null, fileName can be null.
         * @param fileName      Partially non-null file name, must not include directories
         * @param tableName     Non-null database name that will Pumpk1n use
         */
        public Settings(String customJDBCUrl, String fileName, @NonNull String tableName) {
            if (customJDBCUrl == null) {
                if (fileName == null) {
                    throw new IllegalArgumentException("fileName is null! (customJDBCUrl is also null)");
                }
            }

            if (tableName.contains(";")) {
                throw new IllegalArgumentException("TableName " + tableName + " contains semicolon!");
            }

            this.customJDBCUrl = customJDBCUrl;
            this.fileName = fileName;
            this.tableName = tableName;
        }

        public static class Builder {

            private @Getter String fileName = "pumpkin_database.db";
            private @Getter String tableName = "pumpkin";
            private @Getter String customJDBCUrl = null;

            /**
             * Creates empty {@link Builder} with default values
             */
            public Builder() {
            }

            /**
             * Creates empty {@link Builder} with default values
             *
             * @return Non-null {@link Builder}
             */
            public static @NonNull Builder create() {
                return new Builder();
            }

            /**
             * Builds {@link Settings}
             *
             * @return Non-null {@link Settings}
             */
            public @NonNull Settings build() {
                if (customJDBCUrl == null) {
                    if (fileName == null) {
                        throw new IllegalArgumentException("File name was not set. (customJDBCUrl is null)");
                    }
                }

                if (tableName == null) {
                    throw new IllegalArgumentException("Database name was not set.");
                }

                return new Settings(customJDBCUrl, fileName, tableName);
            }

            /**
             * Sets Database's file name
             *
             * @param fileName Non-null {@link String}
             *
             * @return {@link Builder}, useful for chaining
             */
            public @NonNull Builder setFileName(@NonNull String fileName) {
                this.fileName = fileName;
                return this;
            }

            /**
             * Sets table name to use
             *
             * @param tableName Non-null {@link String}
             *
             * @return {@link Builder}, useful for chaining
             */
            public @NonNull Builder setTableName(@NonNull String tableName) {
                this.tableName = tableName;
                return this;
            }

            /**
             * Sets custom JDBC url to use
             *
             * @param customJDBCUrl Non-null {@link String}
             *
             * @return {@link Builder}, useful for chaining
             */
            public @NonNull Builder setCustomJDBCUrl(String customJDBCUrl) {
                this.customJDBCUrl = customJDBCUrl;
                return this;
            }
        }
    }
}
