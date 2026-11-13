package parser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LlmClient {
    public String complete(String prompt) throws Exception {
        String apiKey = System.getenv("LLM_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("LLM_API_KEY is missing");
        }
        String escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n");
        String payload = "{\"model\":\"gpt-4o-mini\",\"messages\":[{\"role\":\"user\",\"content\":\"" + escapedPrompt + "\"}]}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://openai.com"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("LLM API error: " + response.statusCode());
        }
        return response.body();
    }
}
