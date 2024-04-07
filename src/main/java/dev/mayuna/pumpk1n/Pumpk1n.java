package dev.mayuna.pumpk1n;

import dev.mayuna.pumpk1n.api.DataElement;
import dev.mayuna.pumpk1n.api.Migratable;
import dev.mayuna.pumpk1n.api.StorageHandler;
import dev.mayuna.pumpk1n.objects.DataHolder;
import dev.mayuna.pumpk1n.util.BaseLogger;
import dev.mayuna.pumpk1n.util.SLF4JPumpk1nLogger;
import lombok.Getter;
import lombok.NonNull;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.*;

public class Pumpk1n {

    protected final List<DataHolder> dataHolderList = Collections.synchronizedList(new LinkedList<>());
    protected @Getter StorageHandler storageHandler;
    protected @Getter BaseLogger logger = new SLF4JPumpk1nLogger(null, null);

    /**
     * Creates a new {@link Pumpk1n} with the given {@link StorageHandler}
     *
     * @param storageHandler The {@link StorageHandler} to use
     */
    public Pumpk1n(StorageHandler storageHandler) {
        this.storageHandler = storageHandler;
        this.storageHandler.setPumpk1n(this);
    }

    /**
     * Enables SLF4J logging with default DEBUG level.<br>
     * If different logger is desired, extend {@link BaseLogger} and set it with {@link Pumpk1n#setLogger(BaseLogger)}
     */
    public void enableLogging() {
        logger = new SLF4JPumpk1nLogger(LoggerFactory.getLogger(Pumpk1n.class), Level.DEBUG);
    }

    /**
     * Enables SLF4J logging with specified level<br>
     * If different logger is desired, extend {@link BaseLogger} and set it with {@link Pumpk1n#setLogger(BaseLogger)}
     *
     * @param level Non-null logging level
     */
    public void enableLogging(Level level) {
        logger = new SLF4JPumpk1nLogger(LoggerFactory.getLogger(Pumpk1n.class), level);
    }

    /**
     * Sets the logger to use
     *
     * @param logger Non-null {@link BaseLogger}
     */
    public void setLogger(@NonNull BaseLogger logger) {
        this.logger = logger;
    }

    /**
     * Calls current {@link StorageHandler#prepareStorage()}
     */
    public void prepareStorage() {
        logger.logMisc("Preparing storage...");
        storageHandler.prepareStorage();
        logger.logMisc("Storage has been prepared");
    }

    /**
     * Gets {@link DataHolder} by its id
     *
     * @param uuid Non-null {@link UUID}
     *
     * @return Nullable {@link DataHolder}
     */
    public DataHolder getDataHolder(@NonNull UUID uuid) {
        synchronized (dataHolderList) {
            for (DataHolder dataHolder : dataHolderList) {
                if (dataHolder != null) {
                    UUID dataHolderUUID = dataHolder.getUuid();
                    if (dataHolderUUID != null) {
                        if (dataHolderUUID.equals(uuid)) {
                            logger.logRead(dataHolder);
                            return dataHolder;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Gets or loads {@link DataHolder} by its id
     *
     * @param uuid Non-null {@link UUID}
     *
     * @return Nullable {@link DataHolder}
     */
    public DataHolder getOrLoadDataHolder(@NonNull UUID uuid) {
        DataHolder dataHolder = getDataHolder(uuid);

        if (dataHolder == null) {
            dataHolder = storageHandler.loadHolder(uuid);

            if (dataHolder != null) {
                logger.logLoad(dataHolder);
                dataHolderList.add(dataHolder);
            }
        }

        return dataHolder;
    }

    /**
     * Gets or loads {@link DataHolder} by its id. If it did not load, it creates new {@link DataHolder} and loads it.
     *
     * @param uuid Non-null {@link UUID}
     *
     * @return Non-null {@link DataHolder}
     */
    public @NonNull DataHolder getOrCreateDataHolder(@NonNull UUID uuid) {
        DataHolder dataHolder = getOrLoadDataHolder(uuid);

        if (dataHolder == null) {
            dataHolder = new DataHolder(this, uuid);
            dataHolderList.add(dataHolder);
            logger.logCreate(dataHolder);
        }

        return dataHolder;
    }

    /**
     * Adds your {@link DataHolder} into memory if there's no {@link DataHolder} with your {@link DataHolder}'s id
     *
     * @param dataHolder Non-null {@link DataHolder}
     */
    public void addToMemoryDataHolder(@NonNull DataHolder dataHolder) {
        if (getDataHolder(dataHolder.getUuid()) == null) {
            logger.logWrite(dataHolder, "added to memory");
            dataHolderList.add(dataHolder);
        }
    }

    /**
     * Replaces {@link DataHolder} in memory if there's {@link DataHolder} with your {@link DataHolder}'s id, if not, it simply adds specified
     * {@link DataHolder} into memory
     *
     * @param dataHolder Non-null {@link DataHolder}
     */
    public void addOrReplaceDataHolder(@NonNull DataHolder dataHolder) {
        DataHolder anotherDataHolder = getDataHolder(dataHolder.getUuid());

        if (anotherDataHolder != null) {
            dataHolderList.remove(anotherDataHolder);
        }

        dataHolderList.add(dataHolder);
        logger.logWrite(dataHolder, "added/replaced");
    }

    /**
     * Unloads {@link DataHolder} from current storage
     *
     * @param uuid Non-null {@link UUID}
     *
     * @return True if any {@link DataHolder} was unloaded
     */
    public boolean unloadDataHolder(@NonNull UUID uuid) {
        boolean removed = dataHolderList.removeIf(dataHolderFilter -> dataHolderFilter.getUuid().equals(uuid));

        if (removed) {
            logger.logWrite(uuid, "removed from memory");
        }

        return removed;
    }

    /**
     * Unloads and removes {@link DataHolder} from current storage
     *
     * @param uuid Non-null {@link UUID}
     *
     * @return True if removed, false otherwise
     */
    public boolean deleteDataHolder(@NonNull UUID uuid) {
        unloadDataHolder(uuid);
        boolean removed = storageHandler.removeHolder(uuid);

        if (removed) {
            logger.logWrite(uuid, "removed from storage");
        }

        return removed;
    }

    /**
     * Saves {@link DataHolder}
     *
     * @param dataHolder Non-null {@link DataHolder}
     */
    public void saveDataHolder(@NonNull DataHolder dataHolder) {
        logger.logBeforeSave(dataHolder);
        dataHolder.getDataElementMap().values().forEach(DataElement::beforeSave);
        storageHandler.saveHolder(dataHolder);
        logger.logWrite(dataHolder, "saved");
    }

    /**
     * Returns unmodifiable list of {@link DataHolder}s
     *
     * @return Unmodifiable list of {@link DataHolder}
     */
    public @NonNull List<DataHolder> getDataHolderList() {
        return Collections.unmodifiableList(dataHolderList);
    }

    /**
     * Migrates all loaded and unloaded data holders this storage handler has. Current storage handler must implement {@link Migratable} interface,
     * otherwise {@link RuntimeException} is thrown
     *
     * @param storageHandler Non-null {@link StorageHandler} object to migrate to. Must as well implement {@link Migratable} interface
     */
    public void migrateTo(@NonNull StorageHandler storageHandler) {
        if (!(storageHandler instanceof Migratable)) {
            throw new RuntimeException("Storage handler " + storageHandler.getName() + " does not implement " + Migratable.class.getName() + " interface! Cannot migrate into this storage handler.");
        }

        StorageHandler oldStorageHandler = this.storageHandler;
        this.storageHandler = storageHandler;
        this.storageHandler.setPumpk1n(this);
        List<DataHolder> oldDataHolders = new ArrayList<>(this.dataHolderList);
        this.dataHolderList.clear();

        if (!(oldStorageHandler instanceof Migratable)) {
            throw new RuntimeException("Current storage handler " + oldStorageHandler.getClass().getName() + " does not support migrating.");
        }

        Migratable fromMigratable = (Migratable) oldStorageHandler;

        String storageHandlerNameTo = this.storageHandler.getName();
        String storageHandlerNameFrom = oldStorageHandler.getName();

        logger.logMisc("Preparing migration from " + storageHandlerNameFrom + " to " + storageHandlerNameTo);

        this.storageHandler.prepareStorage();
        oldStorageHandler.prepareStorage();

        logger.logMisc("Getting all DataHolder UUIDs from storage handler " + storageHandlerNameFrom);

        List<UUID> uuids = fromMigratable.getAllHolderUUIDs();

        logger.logMisc("Migrating all data holders from " + storageHandlerNameFrom + " to " + storageHandlerNameTo + "...");

        long start = System.currentTimeMillis();

        uuids.forEach(uuid -> {
            try {
                DataHolder dataHolder = oldStorageHandler.loadHolder(uuid);

                if (dataHolder == null) {
                    return;
                }

                this.saveDataHolder(dataHolder);
            } catch (Exception exception) {
                logger.logMisc("Exception occurred while migrating data holder " + uuid + "!", exception);
            }
        });

        oldDataHolders.forEach(dataHolder -> {
            try {
                this.saveDataHolder(dataHolder);
            } catch (Exception exception) {
                logger.logMisc("Exception occurred while migrating data holder " + dataHolder.getUuid() + "!", exception);
            }
        });

        logger.logMisc("Migrating done in " + (System.currentTimeMillis() - start) + " ms");
    }
}
