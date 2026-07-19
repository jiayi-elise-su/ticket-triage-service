package com.triage.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Locale;

/**
 * Router agent: ticket -> (category, priority).
 * NOTE: kept intentionally light — the resume already covers heavy agent/LLM work
 * (AutoGen multi-agent, LLM eval harness, fine-tuning). The value here is different:
 * productionizing an LLM tool-call inside a real backend service (structured output,
 * validation, graceful fallback), not agent sophistication.
 *
 * provider=stub -> rule-based, runs offline. provider=openai -> real GPT call.
 */
@Component
public class TriageAgent {
    private static final Logger log = LoggerFactory.getLogger(TriageAgent.class);
    private static final List<String> CATEGORIES = List.of("billing", "technical", "refund", "escalate");
    private static final List<String> PRIORITIES = List.of("high", "medium", "low");

    private final String provider;
    private final String apiKey;
    private final String model;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();

    public TriageAgent(@Value("${app.llm.provider}") String provider,
                       @Value("${app.llm.openai-api-key}") String apiKey,
                       @Value("${app.llm.openai-model}") String model) {
        this.provider = provider;
        this.apiKey = apiKey;
        this.model = model;
    }

    public record Triage(String category, String priority) {}

    public Triage classify(String subject, String body) {
        if ("openai".equalsIgnoreCase(provider) && apiKey != null && !apiKey.isBlank()) {
            try {
                return openai(subject, body);
            } catch (Exception e) {
                log.warn("openai triage failed, falling back to stub: {}", e.getMessage());
            }
        }
        return stub(subject, body);
    }

    private Triage stub(String subject, String body) {
        String t = (subject + " " + body).toLowerCase(Locale.ROOT);
        String category;
        if (containsAny(t, "refund", "money back", "chargeback")) category = "refund";
        else if (containsAny(t, "charge", "invoice", "billing", "payment")) category = "billing";
        else if (containsAny(t, "error", "crash", "bug", "not working", "login")) category = "technical";
        else category = "escalate";

        String priority;
        if (containsAny(t, "urgent", "asap", "immediately", "down", "can't")) priority = "high";
        else if (t.length() > 200) priority = "medium";
        else priority = "low";
        return new Triage(category, priority);
    }

    private Triage openai(String subject, String body) throws Exception {
        String prompt = "Classify this support ticket. Respond ONLY with JSON "
                + "{\"category\":\"...\",\"priority\":\"...\"}. "
                + "category in " + CATEGORIES + ", priority in " + PRIORITIES
                + ".\n\nSubject: " + subject + "\nBody: " + body;
        String reqBody = mapper.writeValueAsString(java.util.Map.of(
                "model", model,
                "temperature", 0,
                "messages", List.of(java.util.Map.of("role", "user", "content", prompt))));
        HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(reqBody))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode content = mapper.readTree(resp.body())
                .path("choices").path(0).path("message").path("content");
        JsonNode parsed = mapper.readTree(content.asText());
        String category = parsed.path("category").asText("escalate");
        String priority = parsed.path("priority").asText("medium");
        if (!CATEGORIES.contains(category)) category = "escalate";
        if (!PRIORITIES.contains(priority)) priority = "medium";
        return new Triage(category, priority);
    }

    private static boolean containsAny(String s, String... needles) {
        for (String n : needles) if (s.contains(n)) return true;
        return false;
    }
}
