package dev.mayuna.pumpk1n.api;

import com.google.gson.GsonBuilder;
import lombok.NonNull;

/**
 * Interface with which you can implement onto your own data-class implementation
 */
public interface DataElement {

    /**
     * This method is called when this {@link DataElement} is loaded
     */
    default void onLoad() {
        // Empty
    }

    /**
     * This method is called when this {@link DataElement} is just before saving
     */
    default void beforeSave() {
        // Empty
    }

    /**
     * You can override this method in order to create custom {@link GsonBuilder}.<br> For example, if you use
     * {@link com.google.gson.annotations.Expose} annotations, you want to override this method and use
     * {@link GsonBuilder#excludeFieldsWithoutExposeAnnotation()}
     *
     * @return Non-null {@link GsonBuilder}
     */
    default @NonNull GsonBuilder getGsonBuilder() {
        return new GsonBuilder();
    }
}
