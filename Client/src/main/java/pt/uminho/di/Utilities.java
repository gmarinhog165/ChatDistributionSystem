package pt.uminho.di;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for common functions
 */
public class Utilities {

    /**
     * Parse JSON array of strings
     */
    public static List<String> parseJsonStringArray(String json) {
        List<String> result = new ArrayList<>();

        // Simple regex pattern to extract strings from JSON array
        // This is a basic implementation, consider using a proper JSON library in production
        Pattern pattern = Pattern.compile("\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            result.add(matcher.group(1));
        }

        return result;
    }

    /**
     * Parse JSON array of server objects
     */
    public static List<ServerInfo> parseJsonServerArray(String json) {
        List<ServerInfo> result = new ArrayList<>();

        // Simple regex pattern to extract IP and port from JSON objects
        // This is a basic implementation, consider using a proper JSON library in production
        Pattern pattern = Pattern.compile("\\{\"ip\":\"([^\"]+)\",\"port\":(\\d+)\\}");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            String ip = matcher.group(1);
            int port = Integer.parseInt(matcher.group(2));
            result.add(new ServerInfo(ip, port));
        }

        return result;
    }
}
