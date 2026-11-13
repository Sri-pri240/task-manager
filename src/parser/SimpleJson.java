package parser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleJson {
    public static Map<String, String> parseObject(String json) {
        if (json == null || !json.contains("{") || !json.contains("}")) {
            throw new IllegalArgumentException("Invalid JSON format");
        }
        Map<String, String> result = new HashMap<>();
        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|null)");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            result.put(matcher.group(1), matcher.group(2));
        }
        return result;
    }
}
