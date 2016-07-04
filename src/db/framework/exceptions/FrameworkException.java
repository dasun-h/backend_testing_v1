package db.framework.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FrameworkException.java - Parent Framework exception class which is extended by exception class.
 *
 * @author Gimhani Abeykoon
 * @version 1.0-SNAPSHOT Last modified on 06_25_2015
 * @since 05/27/2015
 */
public class FrameworkException extends Exception {
    private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkException.class);

    /**
     * Constructor passing message only
     *
     * @param message error message
     */
    public FrameworkException(String message) {
        super(message);
        LOGGER.error(message);
    }

    /**
     * Constructor passing message and exception object
     *
     * @param message error message
     * @param e       exception object
     */
    public FrameworkException(String message, Exception e) {
        super(message, e);
        LOGGER.error(message, e);
    }


}
