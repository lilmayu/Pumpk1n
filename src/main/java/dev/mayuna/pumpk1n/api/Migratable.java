package dev.mayuna.pumpk1n.api;

import lombok.NonNull;

import java.util.List;
import java.util.UUID;

/**
 * You can implement this interface into your custom storage handlers and make them migratable to other migratable storage handlers
 */
public interface Migratable {

    /**
     * Returns all UUIDs of holders in the storage
     *
     * @return Non-null list of UUIDs
     */
    @NonNull List<UUID> getAllHolderUUIDs();

}
