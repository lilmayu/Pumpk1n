package dev.mayuna.pumpk1n.impl;

import com.google.gson.JsonParser;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.mayuna.pumpk1n.api.StorageHandler;
import dev.mayuna.pumpk1n.objects.DataHolder;
import lombok.Getter;
import lombok.NonNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * SQL based storage
 */
public class SQLStorageHandler extends StorageHandler {

    private final @Getter PoolManager poolManager;
    private final @Getter String tableName;

    public SQLStorageHandler(HikariConfig hikariConfig, String tableName) {
        super(SQLStorageHandler.class.getSimpleName());
        this.poolManager = new PoolManager(hikariConfig);
        this.tableName = tableName;
    }

    /////////////////////
    // Storage Handler //
    /////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareStorage() {
        createDatabase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveHolder(@NonNull DataHolder dataHolder) {
        insertOrReplace(dataHolder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataHolder loadHolder(@NonNull UUID uuid) {
        return loadByUUID(uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeHolder(@NonNull UUID uuid) {
        return delete(uuid);
    }

    /**
     * Closes Hikari Pool
     */
    public void closePool() {
        poolManager.closePool();
    }

    /////////////////
    // SQL Methods //
    /////////////////

    private void createDatabase() {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = poolManager.getConnection();
            statement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                            "uuid VARCHAR(36) PRIMARY KEY NOT NULL," +
                            "data JSON NOT NULL" +
                            ");"
            );

            statement.execute();
        } catch (Exception exception) {
            throw new RuntimeException("Exception occurred while creating " + tableName + " table in SQL database!", exception);
        } finally {
            poolManager.closeAll(connection, statement, null);
        }
    }

    private void insertOrReplace(@NonNull DataHolder dataHolder) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = poolManager.getConnection();
            statement = connection.prepareStatement("REPLACE INTO " + tableName + " (uuid, data) VALUES (?, ?)");
            statement.setString(1, dataHolder.getUuid().toString());
            statement.setString(2, dataHolder.getAsJsonObject().toString());
            statement.executeUpdate();
        } catch (Exception exception) {
            throw new RuntimeException("Exception occurred while saving DataHolder with UUID " + dataHolder.getUuid() + " to SQL database!", exception);
        } finally {
            poolManager.closeAll(connection, statement, null);
        }
    }

    private DataHolder loadByUUID(UUID uuid) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = poolManager.getConnection();
            statement = connection.prepareStatement("SELECT data FROM " + tableName + " WHERE uuid = ?;");
            statement.setString(1, uuid.toString());
            statement.executeQuery();

            resultSet = statement.getResultSet();

            if (resultSet.next()) {
                return DataHolder.loadFromJsonObject(getPumpk1n(), JsonParser.parseString(resultSet.getString("data")).getAsJsonObject());
            }

            return null;
        } catch (Exception exception) {
            throw new RuntimeException("Exception occurred while loading DataHolder with UUID " + uuid + " from SQL database!", exception);
        } finally {
            poolManager.closeAll(connection, statement, resultSet);
        }
    }

    private boolean delete(@NonNull UUID uuid) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = poolManager.getConnection();
            statement = connection.prepareStatement("DELETE FROM " + tableName + " WHERE uuid = ?;");
            statement.setString(1, uuid.toString());

            return statement.executeUpdate() > 0;
        } catch (Exception exception) {
            throw new RuntimeException("Exception occurred while deleting DataHolder with UUID " + uuid + " from SQL database!", exception);
        } finally {
            poolManager.closeAll(connection, statement, null);
        }
    }

    public static class PoolManager {

        private final @Getter HikariDataSource dataSource;

        public PoolManager(HikariConfig config) {
            this.dataSource = new HikariDataSource(config);
        }

        public Connection getConnection() throws SQLException {
            return dataSource.getConnection();
        }

        public void closePool() {
            if (!dataSource.isClosed()) {
                dataSource.close();
            }
        }

        public void closeAll(Connection conn, PreparedStatement ps, ResultSet res) {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ignored) {
                }
            }
            if (res != null) {
                try {
                    res.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }
}