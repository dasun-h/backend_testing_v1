package db.framework.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ApplicationException.java - Handle application related exceptions.
 *
 * @author Gimhani Abeykoon
 * @version 1.0-SNAPSHOT Last modified on 06_05_2015
 * @since 05/27/2015
 */
public class ApplicationException extends FrameworkException {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationException.class);

    /**
     * Constructor passing message only
     *
     * @param message error message
     */
    public ApplicationException(String message) {
        super(message);
        LOGGER.error(message);
    }

    /**
     * Constructor passing message and exception object
     *
     * @param message error message
     * @param e       exception object
     */
    public ApplicationException(String message, Exception e) {
        super(message, e);
        LOGGER.error(message, e);
    }

}

