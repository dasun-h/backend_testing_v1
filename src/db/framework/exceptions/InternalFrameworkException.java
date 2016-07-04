package db.framework.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InternalFrameworkException.java - Handle Internal Framework related exceptions.
 *
 * @author Gimhani Abeykoon
 * @version 1.0-SNAPSHOT
 * @since 07/02/2015
 */
public class InternalFrameworkException extends FrameworkException {
    private static final Logger LOGGER = LoggerFactory.getLogger(InternalFrameworkException.class);

    /**
     * Constructor passing message only
     *
     * @param message error message
     */
    public InternalFrameworkException(String message) {
        super(message);
        LOGGER.error(message);
    }

    /**
     * Constructor passing message and exception object
     *
     * @param message error message
     * @param e       exception object
     */
    public InternalFrameworkException(String message, Exception e) {
        super(message, e);
        LOGGER.error(message, e);
    }

}
