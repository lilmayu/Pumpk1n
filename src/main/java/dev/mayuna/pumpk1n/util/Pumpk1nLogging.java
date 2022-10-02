package dev.mayuna.pumpk1n.util;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

public class Pumpk1nLogging {

    private final @Getter Logger logger;
    private @Getter @Setter int loggingLevel = -1;

    public Pumpk1nLogging() {
        logger = null;
    }

    public Pumpk1nLogging(Logger logger, int loggingLevel) {
        this.logger = logger;
        this.loggingLevel = loggingLevel;
    }

    public void log(String message) {
        log(message, null);
    }

    public void log(Exception exception) {
        log(null, exception);
    }

    public void log(String message, Exception exception) {
        if (logger == null || loggingLevel == -1) {
            return;
        }

        if (message == null && exception != null) {
            message = "Exception occurred!";
        }

        LoggingEventBuilder builder = logger.atLevel(Level.intToLevel(loggingLevel));
        if (exception != null) {
            builder.log(message, exception);
        } else {
            builder.log(message);
        }
    }
}
