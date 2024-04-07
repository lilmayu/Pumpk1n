package dev.mayuna.pumpk1n.api;

import dev.mayuna.pumpk1n.Pumpk1n;
import dev.mayuna.pumpk1n.objects.DataHolder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.UUID;

public abstract class StorageHandler {

    private final @Getter String name;
    private @Getter @Setter Pumpk1n pumpk1n;

    /**
     * Creates StorageHandler with name
     *
     * @param name Non-null string
     */
    public StorageHandler(@NonNull String name) {
        this.name = name;
    }

    /**
     * Prepares the storage
     */
    public abstract void prepareStorage();

    /**
     * Saves specified {@link DataHolder} into storage
     *
     * @param dataHolder Non-null {@link DataHolder}
     */
    public abstract void saveHolder(@NonNull DataHolder dataHolder);

    /**
     * Loads specified {@link DataHolder} by its {@link UUID} from database
     *
     * @param uuid Non-null UUID
     *
     * @return Nullable {@link DataHolder}
     */
    public abstract DataHolder loadHolder(@NonNull UUID uuid);

    /**
     * Removes specified {@link DataHolder} by its {@link UUID} from database
     *
     * @param uuid Non-null UUID
     *
     * @return True if holder was removed, false otherwise
     */
    public abstract boolean removeHolder(@NonNull UUID uuid);
}
