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

public class BufferedFolderStorageHandler extends StorageHandler implements Migratable {

    private final @Getter String folderPath;
    private final @Getter int buffers;
    private File folder;

    public BufferedFolderStorageHandler(String folderPath, int buffers) {
        super(BufferedFolderStorageHandler.class.getSimpleName());

        if (!folderPath.endsWith("/")) {
            folderPath += "/";
        }

        this.folderPath = folderPath;
        this.buffers = buffers;
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
        for (int i = 0; i < buffers; i++) {
            File file = new File(getFileName(dataHolder.getUuid(), i));

            try {
                JsonUtil.saveJson(dataHolder.getAsJsonObject(), file);
            } catch (IOException e) {
                throw new RuntimeException("Could not save Data Holder with UUID " + dataHolder.getUuid() + "!", e);
            }
        }
    }

    @Override
    public DataHolder loadHolder(@NonNull UUID uuid) {
        JsonObject jsonObject = null;
        Exception lastException = null;

        for (int i = 0; i < buffers; i++) {
            File file = new File(getFileName(uuid, i));

            if (!file.exists()) {
                continue;
            }

            try {
                jsonObject = JsonUtil.createOrLoadJsonFromFile(file).getJsonObject();
                break;
            } catch (Exception e) {
                lastException = e;
            }
        }

        if (jsonObject == null) {
            if (lastException == null) {
                return null;
            }

            throw new RuntimeException("Could not load Data Holder with UUID " + uuid + "!", lastException);
        }

        return DataHolder.loadFromJsonObject(getPumpk1n(), jsonObject);
    }

    @Override
    public boolean removeHolder(@NonNull UUID uuid) {
        boolean success = false;
        for (int i = 0; i < buffers; i++) {
            File file = new File(getFileName(uuid, i));

            if (!file.exists()) {
                continue;
            }

            success = file.delete();
        }

        return success;
    }

    private String getFileName(UUID uuid, int iteration) {
        String path = folderPath + uuid.toString();

        if (iteration != 0) {
            path += "_" + iteration;
        }

        return path + ".json";
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
                String fileName = file.getName();
                uuid = UUID.fromString(fileName.replaceFirst("(_\\d*.json)$", ""));
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
