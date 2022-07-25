package dev.mayuna.pumpk1n;

import dev.mayuna.pumpk1n.api.ClassGetter;
import lombok.NonNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Pumpk1nClassHelper {

    private final List<ClassGetter> classGetters = Collections.synchronizedList(new LinkedList<>());

    /**
     * Registers new {@link ClassGetter}
     *
     * @param classGetter Non-null implementation of {@link ClassGetter}
     */
    public void registerNewClassGetter(@NonNull ClassGetter classGetter) {
        classGetters.add(classGetter);
    }

    /**
     * Possibly returns {@link Class} for specified class name. This method iterates over all registered {@link ClassGetter}s and returns first
     * non-null return value. If all results in null, this method uses {@link Class#forName(String)}, if even this method cannot find specified class,
     * this method returns null.
     *
     * @param className Non-null class name
     *
     * @return Nullable {@link Class}
     */
    public Class<?> getClass(@NonNull String className) {
        Class<?> clazz = null;

        synchronized (classGetters) {
            for (ClassGetter classGetter : classGetters) {
                clazz = classGetter.getClass(className);

                if (clazz != null) {
                    return clazz;
                }
            }
        }

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
