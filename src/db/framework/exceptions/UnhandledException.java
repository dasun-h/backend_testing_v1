package db.framework.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UnhandledException.java - Unknown exceptions.
 *
 * @author Gimhani Abeykoon
 * @version 1.0-SNAPSHOT
 * @since 05/27/2015
 */
public class UnhandledException extends FrameworkException {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnhandledException.class);

    /**
     * Constructor passing message only
     *
     * @param message error message
     */
    public UnhandledException(String message) {
        super(message);
        LOGGER.error(message);
    }

    /**
     * Constructor passing message and exception object
     *
     * @param message error message
     * @param e       exception object
     */
    public UnhandledException(String message, Exception e) {
        super(message, e);
        LOGGER.error(message, e);
    }
}

