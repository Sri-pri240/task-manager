package parser;

import exception.BadGatewayException;
import model.ParsedTask;
import java.util.Map;

public class LlmTaskParser implements TaskParser {
    private final LlmClient client;

    public LlmTaskParser(LlmClient client) {
        this.client = client;
    }

    @Override
    public ParsedTask parse(String text, String today) {
        String prompt = "Today is " + today + ". Turn this text into a task: \"" + text + "\". "
                + "Reply ONLY with JSON containing keys: title, description, dueDate (YYYY-MM-DD or null), recurrence (or null).";
        try {
            String raw = client.complete(prompt);
            Map<String, String> data = SimpleJson.parseObject(raw);
            return new ParsedTask(data.get("title"), data.get("description"), data.get("dueDate"), data.get("recurrence"));
        } catch (Exception e) {
            throw new BadGatewayException("LLM failed to parse task: " + e.getMessage());
        }
    }
}

