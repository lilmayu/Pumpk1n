package dev.mayuna.pumpk1n.api;

import java.util.List;
import java.util.UUID;

/**
 * You can implement this interface into your custom storage handlers and make them migratable to other migratable storage handlers
 */
public interface Migratable {

    List<UUID> getAllHolderUUIDs();

}
