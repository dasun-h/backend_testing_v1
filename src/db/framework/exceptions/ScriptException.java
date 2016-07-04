package db.framework.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ScriptException.java - Handle Script related exceptions.
 *
 * @author Gimhani Abeykoon
 * @version 1.0-SNAPSHOT
 * @since 07/02/2015
 */
public class ScriptException extends FrameworkException {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptException.class);

    /**
     * Constructor passing message only
     *
     * @param message error message
     */
    public ScriptException(String message) {
        super(message);
        LOGGER.error(message);
    }

    /**
     * Constructor passing message and exception object
     *
     * @param message error message
     * @param e       exception object
     */
    public ScriptException(String message, Exception e) {
        super(message, e);
        LOGGER.error(message, e);
    }

}
