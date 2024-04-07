package dev.mayuna.pumpk1n.impl;

import com.google.gson.JsonObject;
import dev.mayuna.mayusjsonutils.JsonUtil;
import dev.mayuna.pumpk1n.api.Migratable;
import dev.mayuna.pumpk1n.api.StorageHandler;
import dev.mayuna.pumpk1n.objects.DataHolder;
import lombok.Getter;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Folder based storage
 */
public class FolderStorageHandler extends StorageHandler implements Migratable {

    protected final @Getter String folderPath;
    protected File folder;

    public FolderStorageHandler(@NonNull String folderPath) {
        super(FolderStorageHandler.class.getSimpleName());

        if (!folderPath.endsWith("/")) {
            folderPath += "/";
        }

        this.folderPath = folderPath;
    }

    @Override
    public void prepareStorage() {
        folder = new File(folderPath);

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new RuntimeException("Could not create dirs towards path " + folderPath + "!");
            }
        }
    }

    @Override
    public void saveHolder(@NonNull DataHolder dataHolder) {
        File file = new File(getFileName(dataHolder.getUuid()));

        try {
            JsonUtil.saveJson(dataHolder.getAsJsonObject(), file);
        } catch (IOException e) {
            throw new RuntimeException("Could not save Data Holder with UUID " + dataHolder.getUuid() + "!", e);
        }
    }

    @Override
    public DataHolder loadHolder(@NonNull UUID uuid) {
        File file = new File(getFileName(uuid));

        if (!file.exists()) {
            return null;
        }

        try {
            JsonObject jsonObject = JsonUtil.createOrLoadJsonFromFile(file).getJsonObject();
            return DataHolder.loadFromJsonObject(getPumpk1n(), jsonObject);
        } catch (IOException e) {
            throw new RuntimeException("Could not load Data Holder with UUID " + uuid + "!", e);
        }
    }

    @Override
    public boolean removeHolder(@NonNull UUID uuid) {
        File file = new File(getFileName(uuid));

        if (!file.exists()) {
            return false;
        }

        return file.delete();
    }

    protected String getFileName(@NonNull UUID uuid) {
        return folderPath + uuid.toString() + ".json";
    }

    @Override
    public List<UUID> getAllHolderUUIDs() {
        File folder = new File(folderPath);

        if (!folder.exists()) {
            return new ArrayList<>(0);
        }

        File files[] = folder.listFiles();

        if (files == null) {
            return new ArrayList<>(0);
        }

        List<UUID> uuids = new LinkedList<>();

        for (File file : files) {
            UUID uuid = null;

            try {
                uuid = UUID.fromString(file.getName().replace(".json", ""));
            } catch (IllegalArgumentException ignored) {
            }

            if (uuid != null) {
                if (!uuids.contains(uuid)) {
                    uuids.add(uuid);
                }
            }
        }

        return uuids;
    }
}
