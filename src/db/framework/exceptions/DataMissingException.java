package db.framework.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataMissingException.java - Handle data related exceptions.
 *
 * @author Gimhani Abeykoon
 * @version 1.0-SNAPSHOT
 * @since 07/02/2015
 */
public class DataMissingException extends FrameworkException {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataMissingException.class);

    /**
     * Constructor passing message only
     *
     * @param message error message
     */
    public DataMissingException(String message) {
        super(message);
        LOGGER.error(message);
    }

    /**
     * Constructor passing message and exception object
     *
     * @param message error message
     * @param e       exception object
     */
    public DataMissingException(String message, Exception e) {
        super(message, e);
        LOGGER.error(message, e);
    }

}

