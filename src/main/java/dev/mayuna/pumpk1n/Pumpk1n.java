package dev.mayuna.pumpk1n;

import dev.mayuna.pumpk1n.api.DataElement;
import dev.mayuna.pumpk1n.api.StorageHandler;
import dev.mayuna.pumpk1n.objects.DataHolder;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Pumpk1n {

    private final List<DataHolder> dataHolderList = Collections.synchronizedList(new LinkedList<>());
    private final @Getter Pumpk1nClassHelper classHelper = new Pumpk1nClassHelper();
    private @Getter StorageHandler storageHandler; // not final tho

    public Pumpk1n(StorageHandler storageHandler) {
        this.storageHandler = storageHandler;

        this.storageHandler.setPumpk1n(this);
    }

    /**
     * Calls current {@link StorageHandler#prepareStorage()}
     */
    public void prepareStorage() {
        storageHandler.prepareStorage();
    }

    /**
     * Gets {@link DataHolder} by its id
     *
     * @param uuid Non-null {@link UUID}
     *
     * @return Nullable {@link DataHolder}
     */
    public DataHolder getDataHolder(@NonNull UUID uuid) {
        return dataHolderList.stream().filter(dataHolderFilter -> dataHolderFilter.getUuid().equals(uuid)).findFirst().orElse(null);
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
            dataHolderList.add(dataHolder);
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
        }

        return dataHolder;
    }

    /**
     * Adds your {@link DataHolder} into memory if there's no {@link DataHolder} with your {@link DataHolder}'s id
     *
     * @param dataHolder Non-null {@link DataHolder}
     */
    public @NonNull void addToMemoryDataHolder(@NonNull DataHolder dataHolder) {
        if (getDataHolder(dataHolder.getUuid()) == null) {
            dataHolderList.add(dataHolder);
        }
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
        dataHolderList.removeIf(dataHolderFilter -> dataHolderFilter.getUuid().equals(uuid));
        return storageHandler.removeHolder(uuid);
    }

    /**
     * Saves {@link DataHolder}
     *
     * @param dataHolder Non-null {@link DataHolder}
     */
    public void saveDataHolder(@NonNull DataHolder dataHolder) {
        dataHolder.getDataElementMap().values().forEach(DataElement::beforeSave);
        storageHandler.saveHolder(dataHolder);
    }

    /**
     * Returns unmodifiable list of {@link DataHolder}s
     * @return Unmodifiable list of {@link DataHolder}
     */
    public @NonNull List<DataHolder> getDataHolderList() {
        return Collections.unmodifiableList(dataHolderList);
    }
}
