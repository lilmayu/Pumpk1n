package dev.mayuna.pumpk1n.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

/**
 * A logger that uses SLF4J
 */
public class SLF4JPumpk1nLogger extends BaseLogger {

    protected @Getter @Setter Logger logger;
    protected @Getter @Setter Level level;

    /**
     * Creates a new {@link SLF4JPumpk1nLogger} with the given logger and level
     *
     * @param logger The logger to use
     * @param level  The level to use
     */
    public SLF4JPumpk1nLogger(Logger logger, Level level) {
        this.logger = logger;
        this.level = level;
    }

    /**
     * Logs a message with an exception
     *
     * @param message   The message to log
     * @param throwable The exception to log (nullable)
     */
    @Override
    public void log(@NonNull String message, Throwable throwable) {
        if (logger == null || level == null) {
            return;
        }

        LoggingEventBuilder builder = logger.atLevel(level)
                                            .addMarker(MarkerFactory.getMarker("Pumpk1n"));

        if (throwable != null) {
            builder.log(message, throwable);
        } else {
            builder.log(message);
        }
    }
}
