package dev.mayuna.pumpk1n;

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
     * Gets or creates {@link DataHolder} by its id
     *
     * @param uuid Non-null {@link UUID}
     *
     * @return Non-null {@link DataHolder}
     */
    public @NonNull DataHolder getOrCreateDataHolder(@NonNull UUID uuid) {
        DataHolder dataHolder = getDataHolder(uuid);

        if (dataHolder == null) {
            dataHolder = storageHandler.loadHolder(uuid);

            if (dataHolder == null) {
                dataHolder = new DataHolder(this, uuid);
                dataHolderList.add(dataHolder);
                storageHandler.saveHolder(dataHolder);
            }
        }

        return dataHolder;
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
     * Removes {@link DataHolder} from current storage
     *
     * @param uuid Non-null {@link UUID}
     *
     * @return True if removed, false otherwise
     */
    public boolean deleteDataHolder(@NonNull UUID uuid) {
        return storageHandler.removeHolder(uuid);
    }

    public void save(DataHolder dataHolder) {
        storageHandler.saveHolder(dataHolder);
    }
}
