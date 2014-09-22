package it.jrc.form;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility for captilizing strings and splitting camel case property names.
 * 
 * Processed values are cached in a thread-safe map as the regex-replacement is
 * an expensive operation.
 * 
 * @author will
 * 
 */
public class AdminStringUtil {

    private static final int TRUNCATE_LENGTH = 60;

    public static Map<String, String> done = new ConcurrentHashMap<String, String>(
            20, 0.9f, 2);

    public static final String CC_REPLACE = "(?<=[A-Z])(?=[A-Z][a-z])|(?<=[^A-Z])(?=[A-Z])|(?<=[A-Za-z])(?=[^A-Za-z])";

    public static String splitCamelCase(String s) {

        String displayString = done.get(s);

        if (displayString == null) {

            String caps = capitalize(s);

            if (caps.contains("_")) {
                caps = caps.replace("_", "");
            }
            displayString = caps.replaceAll(CC_REPLACE, " ");
            done.put(s, displayString);
        }
        return displayString;

    }

    public static String truncateString(String name) {
        if (name != null && name.length() > TRUNCATE_LENGTH) {
            return name.substring(0, TRUNCATE_LENGTH) + " ...";
        }
        return name;
    }

    public static String capitalize(String str) {

        if (str == null || str.isEmpty()) {
            return "";
        }

        char[] buffer = str.toCharArray();
        buffer[0] = Character.toTitleCase(buffer[0]);
        
        return new String(buffer);
    }

}
