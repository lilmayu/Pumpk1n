package dev.mayuna.pumpk1n;

import dev.mayuna.pumpk1n.api.DataElement;
import dev.mayuna.pumpk1n.api.StorageHandler;
import dev.mayuna.pumpk1n.objects.DataHolder;
import dev.mayuna.pumpk1n.util.Pumpk1nLogging;
import lombok.Getter;
import lombok.NonNull;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Pumpk1n {

    private final List<DataHolder> dataHolderList = Collections.synchronizedList(new LinkedList<>());
    private @Getter StorageHandler storageHandler; // not final tho
    private @Getter Pumpk1nLogging logging = new Pumpk1nLogging(null, -1);

    public Pumpk1n(StorageHandler storageHandler) {
        this.storageHandler = storageHandler;

        this.storageHandler.setPumpk1n(this);
    }

    /**
     * Enables SLF4J logging with default DEBUG level
     */
    public void enableLogging() {
        logging = new Pumpk1nLogging(LoggerFactory.getLogger(Pumpk1n.class), Level.DEBUG.toInt());
    }

    /**
     * Enables SLF4J logging with specified level
     * @param level Non-null logging level
     */
    public void enableLogging(Level level) {
        logging = new Pumpk1nLogging(LoggerFactory.getLogger(Pumpk1n.class), level.toInt());
    }

    /**
     * Enables SLF4J logging with specified level
     * @param level Non-null logging level
     */
    public void enableLogging(int level) {
        logging = new Pumpk1nLogging(LoggerFactory.getLogger(Pumpk1n.class), level);
    }

    /**
     * Calls current {@link StorageHandler#prepareStorage()}
     */
    public void prepareStorage() {
        logging.log("Preparing storage...");
        storageHandler.prepareStorage();
        logging.log("Storage has been prepared");
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
                logging.log("DataHolder with UUID " + uuid + " has been loaded");
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
            logging.log("Creating DataHolder with UUID " + uuid + "...");
            dataHolder = new DataHolder(this, uuid);
            dataHolderList.add(dataHolder);
            logging.log("DataHolder with UUID " + uuid + " has been created");
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
    }

    /**
     * Unloads {@link DataHolder} from current storage
     *
     * @param uuid Non-null {@link UUID}
     *
     * @return True if any {@link DataHolder} was unloaded
     */
    public boolean unloadDataHolder(@NonNull UUID uuid) {
        return dataHolderList.removeIf(dataHolderFilter -> dataHolderFilter.getUuid().equals(uuid));
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
        return storageHandler.removeHolder(uuid);
    }

    /**
     * Saves {@link DataHolder}
     *
     * @param dataHolder Non-null {@link DataHolder}
     */
    public void saveDataHolder(@NonNull DataHolder dataHolder) {
        logging.log("Saving DataHolder with UUID " + dataHolder.getUuid() + "...");
        dataHolder.getDataElementMap().values().forEach(DataElement::beforeSave);
        storageHandler.saveHolder(dataHolder);
        logging.log("DataHolder with UUID " + dataHolder.getUuid() + " has been saved");
    }

    /**
     * Returns unmodifiable list of {@link DataHolder}s
     *
     * @return Unmodifiable list of {@link DataHolder}
     */
    public @NonNull List<DataHolder> getDataHolderList() {
        return Collections.unmodifiableList(dataHolderList);
    }
}
