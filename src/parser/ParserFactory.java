package parser;

import exception.ServiceUnavailableException;

public class ParserFactory {
    public static TaskParser getTaskParser(boolean useLlm, LlmClient client) {
        if (!useLlm) {
            return new RuleBasedTaskParser();
        }
        String key = System.getenv("LLM_API_KEY");
        if (key == null || key.isBlank()) {
            throw new ServiceUnavailableException("LLM_API_KEY environment variable is missing");
        }
        return new LlmTaskParser(client);
    }

    public static BreakdownStrategy getBreakdownStrategy(boolean useLlm, LlmClient client) {
        if (!useLlm) {
            return new RuleBasedBreakdownStrategy();
        }
        String key = System.getenv("LLM_API_KEY");
        if (key == null || key.isBlank()) {
            throw new ServiceUnavailableException("LLM_API_KEY environment variable is missing");
        }
        return new LlmBreakdownStrategy(client);
    }
}
