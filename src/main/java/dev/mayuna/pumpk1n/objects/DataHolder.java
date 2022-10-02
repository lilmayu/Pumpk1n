package dev.mayuna.pumpk1n.objects;

import com.google.gson.*;
import dev.mayuna.pumpk1n.Pumpk1n;
import dev.mayuna.pumpk1n.api.DataElement;
import dev.mayuna.pumpk1n.api.ParentedDataElement;
import lombok.Getter;
import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;

public class DataHolder {

    private final @Getter UUID uuid;
    private final @Getter Pumpk1n pumpk1n;

    private final Map<Class<?>, DataElement> dataElementMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, JsonObject> safeDataElementMap = Collections.synchronizedMap(new HashMap<>());

    public DataHolder(Pumpk1n pumpk1n, UUID uuid) {
        this.pumpk1n = pumpk1n;
        this.uuid = uuid;
    }

    /**
     * It takes a JSON object and converts it into a {@link DataHolder} object
     *
     * @param pumpk1n    The instance of Pumpk1n.
     * @param jsonObject The JsonObject to load the {@link DataHolder} from.
     *
     * @return A new {@link DataHolder} object.
     */
    public static @NonNull DataHolder loadFromJsonObject(Pumpk1n pumpk1n, @NonNull JsonObject jsonObject) {
        return new GsonBuilder().registerTypeAdapter(DataHolder.class, new DataHolderTypeAdapter(pumpk1n))
                                .create()
                                .fromJson(jsonObject, DataHolder.class);
    }

    private static void setDataHolderParent(DataHolder dataHolder, DataElement dataElement) {
        if (dataElement instanceof ParentedDataElement) {
            ((ParentedDataElement) dataElement).setDataHolderParent(dataHolder);
        }
    }

    /**
     * It converts the {@link DataHolder} object into a JsonObject
     *
     * @return A JsonObject
     */
    public @NonNull JsonObject getAsJsonObject() {
        return new GsonBuilder().registerTypeAdapter(DataHolder.class, new DataHolderTypeAdapter(pumpk1n))
                                .create()
                                .toJsonTree(this)
                                .getAsJsonObject();
    }

    /**
     * Gets or creates specified {@link DataElement} by your type {@link T}. Your {@link DataElement} must have at-least one public no-args
     * constructor or this method will result in {@link RuntimeException}
     *
     * @param dataElementClass Non-null class of implementation of your {@link DataElement}
     * @param <T>              Your implementation of {@link DataElement}
     *
     * @return Non-null implementation of your {@link DataElement}
     */
    public <T extends DataElement> T getOrCreateDataElement(@NonNull Class<T> dataElementClass) {
        T dataElement = getDataElement(dataElementClass);

        if (dataElement != null) {
            return dataElement;
        }

        try {
            dataElement = dataElementClass.getConstructor().newInstance();

            setDataHolderParent(this, dataElement);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to create new instance of DataElement " + dataElementClass.getName() + "!", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Missing public no-args constructor in DataElement " + dataElementClass.getName() + "!", e);
        }

        synchronized (dataElementMap) {
            dataElementMap.put(dataElementClass, dataElement);
        }

        return dataElement;
    }

    /**
     * Gets specified {@link DataElement} by your type {@link T}.
     *
     * @param dataElementClass Non-null class of implementation of your {@link DataElement}
     * @param <T>              Your implementation of {@link DataElement}
     *
     * @return Nullable implementation of your {@link DataElement}
     */
    public <T extends DataElement> T getDataElement(@NonNull Class<T> dataElementClass) {
        synchronized (dataElementMap) {
            for (Map.Entry<Class<?>, DataElement> entry : dataElementMap.entrySet()) {
                if (entry.getKey().equals(dataElementClass)) {
                    return (T) entry.getValue();
                }
            }
        }

        synchronized (safeDataElementMap) {
            for (Map.Entry<String, JsonObject> entry : safeDataElementMap.entrySet()) {
                if (entry.getKey().equals(dataElementClass.getName())) {
                    T data = createInstance(dataElementClass, entry.getValue());
                    dataElementMap.put(dataElementClass, data);
                    return data;
                }
            }
        }

        return null;
    }

    /**
     * Removes specified {@link DataElement} by your type {@link T}.
     *
     * @param dataElementClass Non-null class of implementation of your {@link DataElement}
     * @param <T>              Your implementation of {@link DataElement}
     *
     * @return Returns true, if any {@link DataElement} was removed, otherwise false
     */
    public <T extends DataElement> boolean removeDataElement(@NonNull Class<T> dataElementClass) {
        boolean success = false;

        synchronized (dataElementMap) {
            Iterator<Map.Entry<Class<?>, DataElement>> iterator = dataElementMap.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<Class<?>, DataElement> entry = iterator.next();

                if (entry.getKey().equals(dataElementClass)) {
                    iterator.remove();
                    success = true;
                }
            }
        }

        synchronized (safeDataElementMap) {
            Iterator<Map.Entry<String, JsonObject>> iterator = safeDataElementMap.entrySet().iterator();

            while(iterator.hasNext()) {
                Map.Entry<String, JsonObject> entry = iterator.next();

                if (entry.getKey().equals(dataElementClass.getName())) {
                    iterator.remove();
                    success = true;
                }
            }
        }

        return success;
    }

    /**
     * Adds or replaces existing {@link DataElement}
     * @param dataElement Non-null {@link DataElement}
     */
    public void addOrReplaceDataElement(@NonNull DataElement dataElement) {
        synchronized (dataElementMap) {
            Iterator<Map.Entry<Class<?>, DataElement>> iterator = dataElementMap.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<Class<?>, DataElement> entry = iterator.next();

                if (entry.getKey().getName().equals(dataElement.getClass().getName())) {
                    iterator.remove();
                    break;
                }

            }
        }

        dataElementMap.put(dataElement.getClass(), dataElement);
    }

    /**
     * Deletes {@link DataHolder}
     *
     * @return True if removed, false otherwise
     */
    public boolean delete() {
        return pumpk1n.deleteDataHolder(uuid);
    }

    /**
     * Returns all loaded {@link DataElement}s in unmodifiable map
     *
     * @return A unmodifiable map of the data elements
     */
    public Map<Class<?>, DataElement> getDataElementMap() {
        return Collections.unmodifiableMap(dataElementMap);
    }

    /**
     * Saves {@link DataHolder}
     */
    public void save() {
        pumpk1n.saveDataHolder(this);
    }

    private Map<String, JsonObject> getAllDataIntoSafeMap() {
        Map<String, JsonObject> safeDataMap = new HashMap<>();

        dataElementMap.forEach((clazz, dataElement) -> {
            safeDataMap.put(clazz.getName(), dataElement.getGsonBuilder().create().toJsonTree(dataElement).getAsJsonObject());
        });

        safeDataElementMap.forEach((className, jsonData) -> {
            if (!safeDataMap.containsKey(className)) {
                safeDataMap.put(className, jsonData);
            }
        });

        return safeDataMap;
    }

    private <T extends DataElement> T createInstance(Class<T> clazz, JsonObject jsonData) {
        GsonBuilder gsonBuilder;

        try {
            gsonBuilder = clazz.getConstructor().newInstance().getGsonBuilder();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to create new instance of class " + clazz.getName() + "!", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Missing public no-args constructor within class " + clazz.getName() + "!", e);
        }

        T dataElement = gsonBuilder.create().fromJson(jsonData, clazz);
        setDataHolderParent(this, dataElement);
        dataElement.onLoad();
        return dataElement;
    }

    private static class DataHolderTypeAdapter implements JsonSerializer<DataHolder>, JsonDeserializer<DataHolder> {

        private final @Getter Pumpk1n pumpk1n;

        private DataHolderTypeAdapter(Pumpk1n pumpk1n) {
            this.pumpk1n = pumpk1n;
        }

        @Override
        public DataHolder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            DataHolder dataHolder = new DataHolder(pumpk1n, UUID.fromString(jsonObject.get("uuid").getAsString()));

            JsonArray jsonArray = jsonObject.getAsJsonArray("dataMap");
            for (JsonElement jsonElement : jsonArray) {
                JsonObject mapEntryJsonObject = jsonElement.getAsJsonObject();

                String className = mapEntryJsonObject.get("class").getAsString();
                JsonObject jsonData = mapEntryJsonObject.get("data").getAsJsonObject();

                dataHolder.safeDataElementMap.put(className, jsonData);
            }

            return dataHolder;
        }

        @Override
        public JsonElement serialize(DataHolder src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("uuid", src.uuid.toString());

            JsonArray jsonArray = new JsonArray();
            for (Map.Entry<String, JsonObject> entry : src.getAllDataIntoSafeMap().entrySet()) {
                JsonObject entryJsonObject = new JsonObject();

                entryJsonObject.addProperty("class", entry.getKey());
                entryJsonObject.add("data", entry.getValue());

                jsonArray.add(entryJsonObject);

            }
            jsonObject.add("dataMap", jsonArray);
            return jsonObject;
        }
    }
}
