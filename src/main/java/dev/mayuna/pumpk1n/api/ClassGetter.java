package dev.mayuna.pumpk1n.api;

public interface ClassGetter {

    /**
     * Gets {@link Class} by its name
     * @param className Class name
     * @return Nullable {@link Class} object
     */
    Class<?> getClass(String className);

}
