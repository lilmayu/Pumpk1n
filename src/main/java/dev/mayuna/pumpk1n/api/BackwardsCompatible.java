package dev.mayuna.pumpk1n.api;

import lombok.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as backwards compatible with another class
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BackwardsCompatible {

    /**
     * Fully qualified class name, ex. dev.mayuna.pumpk1n.api.DataElement
     *
     * @return Non-null fully qualified class name
     */
    @NonNull String className();

    /**
     * Fully qualified class names, ex. dev.mayuna.pumpk1n.api.DataElement
     *
     * @return Non-null fully qualified class names
     */
    @NonNull String[] classNames() default {};

}
