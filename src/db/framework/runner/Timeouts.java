package db.framework.runner;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dasunh on 2/6/2017.
 */
public class Timeouts {

    private Timeouts() {}

    private static final int DEFAULT_GENERAL_TIMEOUT = 5;
    private static final int DEFAULT_UNTIL_ELEMENT_PRESENT_TIMEOUT = 5;
    private static final String GENERAL_TIMEOUT_KEY = "general_timeout";
    private static final String UNTIL_ELEMENT_PRESENT_TIMEOUT_KEY = "until_element_present_timeout";

    private static Map<String, Integer> timeouts = new HashMap<>();
    private static Timeouts instance;

    /**
     * Gets Default wait Time for element
     *
     * @return Default wait Time for element
     */
    public int untilElementPresent() {
        return getTimeout(UNTIL_ELEMENT_PRESENT_TIMEOUT_KEY, DEFAULT_UNTIL_ELEMENT_PRESENT_TIMEOUT);
    }

    /**
     * Gets Default general wait Time
     *
     * @return Default general wait Time
     */
    public int general() {
        return getTimeout(GENERAL_TIMEOUT_KEY, DEFAULT_GENERAL_TIMEOUT);
    }

    /**
     * initiate Timeouts instance if not initiated already or reuse the existing one
     *
     * @return Timeouts instance
     */
    public static Timeouts instance() {
        if (instance == null) {
            instance = new Timeouts();
        }
        return instance;
    }

    /**
     * fetch the timeout in seconds using key from execution environment
     *
     * @param key key passed in execution for timeout
     * @param defaultSeconds default timeout seconds
     *
     * @return timeout value in seconds for the key asked
     */
    private int getTimeout(String key, int defaultSeconds) {
        if (!timeouts.containsKey(key)) {
            String customValue = MainRunner.getEnvOrExParam(key);
            int timeout;
            if (customValue != null && customValue.matches("^\\d+$")) {
                timeout = Integer.parseInt(customValue);
            } else {
                timeout = defaultSeconds;
            }
            timeouts.put(key, timeout);
        }
        return timeouts.get(key);
    }
}
