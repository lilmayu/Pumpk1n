package dev.mayuna.pumpk1n.objects;

import com.google.gson.*;
import dev.mayuna.pumpk1n.Pumpk1n;
import dev.mayuna.pumpk1n.api.DataElement;
import lombok.Getter;
import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;

public class DataHolder {

    private final @Getter UUID uuid;
    private final @Getter Pumpk1n pumpk1n;
    private final Map<Class<?>, DataElement> dataElementMap = Collections.synchronizedMap(new HashMap<>());

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
        synchronized (dataElementMap) {
            Iterator<Map.Entry<Class<?>, DataElement>> iterator = dataElementMap.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<Class<?>, DataElement> entry = iterator.next();

                if (entry.getKey().equals(dataElementClass)) {
                    iterator.remove();
                    return true;
                }
            }
        }

        return false;
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
     * Saves {@link DataHolder}
     */
    public void save() {
        pumpk1n.saveDataHolder(this);
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
                String clazzToLoad = mapEntryJsonObject.get("class").getAsString();

                Class<?> clazz = pumpk1n.getClassHelper().getClass(clazzToLoad);

                if (clazz == null) {
                    throw new RuntimeException("Could not find class named " + clazzToLoad + "!");
                }

                GsonBuilder gsonBuilder;

                try {
                    gsonBuilder = ((DataElement) clazz.getConstructor().newInstance()).getGsonBuilder();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Unable to create new instance of class " + clazzToLoad + "!", e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("Missing public no-args constructor within class " + clazzToLoad + "!", e);
                }

                DataElement dataElement = (DataElement) gsonBuilder.create().fromJson(mapEntryJsonObject.get("data").getAsJsonObject(), clazz);
                dataHolder.dataElementMap.put(clazz, dataElement);
            }

            return dataHolder;
        }

        @Override
        public JsonElement serialize(DataHolder src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("uuid", src.uuid.toString());

            JsonArray jsonArray = new JsonArray();
            for (Map.Entry<Class<?>, DataElement> entry : src.dataElementMap.entrySet()) {
                Class<?> clazz = entry.getKey();
                GsonBuilder gsonBuilder;

                try {
                    gsonBuilder = ((DataElement) clazz.getConstructor().newInstance()).getGsonBuilder();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Unable to create new instance of class " + clazz.getName() + "!", e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("Missing public no-args constructor within class " + clazz.getName() + "!", e);
                }

                JsonObject entryJsonObject = new JsonObject();

                entryJsonObject.addProperty("class", entry.getKey().getName());
                entryJsonObject.add("data", gsonBuilder.create().toJsonTree(entry.getValue()));

                jsonArray.add(entryJsonObject);

            }
            jsonObject.add("dataMap", jsonArray);
            return jsonObject;
        }
    }
}
