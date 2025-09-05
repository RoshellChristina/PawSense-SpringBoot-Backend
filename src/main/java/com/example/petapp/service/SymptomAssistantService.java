package com.example.petapp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SymptomAssistantService {

    private static final Logger log = LoggerFactory.getLogger(SymptomAssistantService.class);

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private List<String> canonicalSymptoms = Collections.emptyList();
    private static final String GITHUB_AI_ENDPOINT = "https://models.github.ai/inference/chat/completions";
    private static final String MODEL_NAME = "openai/gpt-4.1";

    @Value("${github.ai.token}")
    private String githubToken;

    // load symptoms.json at startup
    @PostConstruct
    public void init() {
        try {
            ClassPathResource r = new ClassPathResource("disease_prediction/symptoms.json");
            try (InputStream is = r.getInputStream()) {
                canonicalSymptoms = mapper.readValue(is, new TypeReference<List<String>>() {});
            }
        } catch (Exception ex) {
            log.error("Failed to load canonical symptoms from resources/disease_prediction/symptoms.json", ex);
            canonicalSymptoms = Collections.emptyList();
        }
    }

    public Map<String, Object> suggestSymptoms(String userText) throws Exception {
        if (userText == null || userText.isBlank()) {
            return Map.of("explanation", "", "suggestions", Collections.emptyList());
        }

        if (githubToken == null || githubToken.isBlank()) {
            log.warn("No github.ai.token configured - cannot call GitHub AI");
            return Map.of("explanation", "AI integration not configured", "suggestions", Collections.emptyList());
        }

        // short canonical list for prompt (avoid huge prompts)
        String symptomListForPrompt = canonicalSymptoms.stream()
                .limit(200)
                .collect(Collectors.joining(", "));

        String system = "You are an assistant that given a user's plain-language description returns ONLY valid JSON " +
                "in the following format: {\"suggestions\":[\"Symptom Name 1\",\"Symptom Name 2\"]}. " +
                "Suggestions must exactly match the provided canonical symptom names. Provide at most 5 suggestions.";

        String userPrompt = "User description: \"" + userText + "\"\n\n" +
                "Canonical symptom names (choose exact names): " + symptomListForPrompt + "\n\n" +
                "Return JSON only.";

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL_NAME);
        body.put("temperature", 0.0);
        body.put("top_p", 1);
        body.put("max_tokens", 300);
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", userPrompt)
        );
        body.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(githubToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = rest.postForEntity(GITHUB_AI_ENDPOINT, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.warn("suggestSymptoms: GitHub AI returned non-2xx {} - body: {}", response.getStatusCode(), response.getBody());
            return Map.of("explanation", "AI service error", "suggestions", Collections.emptyList());
        }

        String resp = response.getBody();
        if (resp == null) {
            return Map.of("explanation", "", "suggestions", Collections.emptyList());
        }

        JsonNode root = mapper.readTree(resp);
        JsonNode choices = root.path("choices");
        String content = "";
        if (choices.isArray() && choices.size() > 0) {
            JsonNode first = choices.get(0);
            if (first.has("message") && first.path("message").has("content")) {
                content = first.path("message").path("content").asText("");
            } else if (first.has("text")) {
                content = first.path("text").asText("");
            } else {
                content = root.toString();
            }
        } else {
            content = root.toString();
        }

        String json = extractJsonObject(content);
        if (json == null) {
            log.warn("suggestSymptoms: failed to extract JSON from model content: {}", content);
            // return explanation as fallback so frontend can show it
            return Map.of("explanation", content, "suggestions", Collections.emptyList());
        }

        JsonNode outNode = mapper.readTree(json);
        String explanation = outNode.path("explanation").asText("");
        List<String> suggestions = new ArrayList<>();
        if (outNode.has("suggestions") && outNode.get("suggestions").isArray()) {
            for (JsonNode n : outNode.get("suggestions")) {
                String candidate = n.asText();
                String mapped = mapToCanonical(candidate);
                if (mapped != null && !suggestions.contains(mapped)) {
                    suggestions.add(mapped);
                }
                if (suggestions.size() >= 5) break;
            }
        }

        return Map.of("explanation", explanation, "suggestions", suggestions);
    }

    // helpers
    private String extractJsonObject(String text) {
        if (text == null) return null;
        int open = text.indexOf('{');
        int close = text.lastIndexOf('}');
        if (open >= 0 && close > open) {
            return text.substring(open, close + 1);
        }
        return null;
    }

    private String mapToCanonical(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        // exact case-insensitive match
        for (String c : canonicalSymptoms) if (c.equalsIgnoreCase(trimmed)) return c;
        // fuzzy match using commons-text (optional); fallback to contains check
        try {
            org.apache.commons.text.similarity.JaroWinklerDistance jw =
                    new org.apache.commons.text.similarity.JaroWinklerDistance();
            String best = null;
            Double bestScore = 0.0;
            for (String c : canonicalSymptoms) {
                Double score = jw.apply(c.toLowerCase(), trimmed.toLowerCase());
                if (score != null && score > bestScore) {
                    bestScore = score;
                    best = c;
                }
            }
            return bestScore >= 0.88 ? best : null;
        } catch (Throwable t) {
            // fallback simple contains
            for (String c : canonicalSymptoms) {
                String a = c.toLowerCase();
                if (a.contains(trimmed.toLowerCase()) || trimmed.toLowerCase().contains(a)) {
                    return c;
                }
            }
            return null;
        }
    }
}
