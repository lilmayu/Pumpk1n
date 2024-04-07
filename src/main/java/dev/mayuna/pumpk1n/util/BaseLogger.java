package dev.mayuna.pumpk1n.util;

import dev.mayuna.pumpk1n.objects.DataHolder;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Data
public abstract class BaseLogger {

    protected boolean logMisc = true;
    protected boolean logLoad = false;
    protected boolean logRead = false;
    protected boolean logWrite = false;
    protected boolean logCreate = false;

    /**
     * Logs a message with an exception
     *
     * @param message   The message to log
     * @param throwable The exception to log (nullable)
     */
    public abstract void log(@NonNull String message, Throwable throwable);

    /**
     * Logs a message
     *
     * @param message The message to log
     */
    public void log(@NonNull String message) {
        log(message, null);
    }

    /**
     * Logs an exception
     *
     * @param throwable The exception to log
     */
    public void log(@NonNull Throwable throwable) {
        log("Exception occurred.", throwable);
    }

    /**
     * Logs a message if {@link BaseLogger#logMisc} is true
     *
     * @param message The message to log
     */
    public void logMisc(@NonNull String message) {
        if (logMisc) {
            log(message);
        }
    }

    /**
     * Logs a message if {@link BaseLogger#logMisc} is true
     *
     * @param message   The message to log
     * @param throwable The exception to log
     */
    public void logMisc(@NonNull String message, Throwable throwable) {
        if (logMisc) {
            log(message, throwable);
        }
    }

    /**
     * Logs a message if {@link BaseLogger#logRead} is true
     *
     * @param dataHolder The loaded {@link DataHolder}
     */
    public void logLoad(@NonNull DataHolder dataHolder) {
        if (logLoad) {
            log("DataHolder with UUID '" + dataHolder.getUuid() + "' has been loaded");
        }
    }

    /**
     * Logs a message if {@link BaseLogger#logRead} is true
     *
     * @param dataHolder The read {@link DataHolder}
     */
    public void logRead(@NonNull DataHolder dataHolder) {
        if (logRead) {
            log("DataHolder with UUID '" + dataHolder.getUuid() + "' has been read");
        }
    }

    /**
     * Logs a message if {@link BaseLogger#logWrite} is true
     *
     * @param dataHolder The written {@link DataHolder}
     * @param action     The action that was performed (e.g. "written")
     */
    public void logWrite(@NonNull DataHolder dataHolder, @NonNull String action) {
        if (logWrite) {
            log("DataHolder with UUID '" + dataHolder.getUuid() + "' has been " + action);
        }
    }

    /**
     * Logs a message if {@link BaseLogger#logWrite} is true
     *
     * @param uuid   The UUID of the {@link DataHolder}
     * @param action The action that was performed (e.g. "written")
     */
    public void logWrite(@NonNull UUID uuid, String action) {
        if (logWrite) {
            log("DataHolder with UUID '" + uuid + "' has been " + action);
        }
    }

    /**
     * Logs a message if {@link BaseLogger#logWrite} is true
     *
     * @param dataHolder The {@link DataHolder} that is about to be saved
     */
    public void logBeforeSave(@NonNull DataHolder dataHolder) {
        if (logWrite) {
            log("DataHolder with UUID '" + dataHolder.getUuid() + "' is about to be saved");
        }
    }

    /**
     * Logs a message if {@link BaseLogger#logCreate} is true
     *
     * @param dataHolder The created {@link DataHolder}
     */
    public void logCreate(@NonNull DataHolder dataHolder) {
        if (logCreate) {
            log("DataHolder with UUID '" + dataHolder.getUuid() + "' has been created");
        }
    }
}
