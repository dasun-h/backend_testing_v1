package db.framework.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EnvironmentException.java - Handle Environment related exceptions.
 *
 * @author Gimhani Abeykoon
 * @version 1.0-SNAPSHOT
 * @since 07/02/2015
 */
public class EnvironmentException extends FrameworkException {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentException.class);

    /**
     * Constructor passing message only
     *
     * @param message error message
     */
    public EnvironmentException(String message) {
        super(message);
        LOGGER.error(message);
    }

    /**
     * Constructor passing message and exception object
     *
     * @param message error message
     * @param e       exception object
     */
    public EnvironmentException(String message, Exception e) {
        super(message, e);
        LOGGER.error(message, e);
    }

}
