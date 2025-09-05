package com.example.petapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiseaseAdviceService {

    @Value("${github.ai.token.new}")
    private String hfToken; // GitHub AI (OpenAI) token

    private static final String GITHUB_AI_ENDPOINT = "https://models.inference.ai.azure.com/chat/completions";
    private static final String MODEL_NAME = "gpt-4.1"; // model name

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateAdvice(String disease) {
        // Build request body
        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL_NAME);

        // Chat messages
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", "You are a helpful pet care assistant."),
                Map.of("role", "user", "content", "Give brief pet care advice if the dog is diagnosed with"
                        + disease + ", and provide sources to educational links as well.")
        );

        body.put("messages", messages);

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(hfToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    GITHUB_AI_ENDPOINT,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode contentNode = root.path("choices").get(0).path("message").path("content");

                if (contentNode != null && !contentNode.isMissingNode()) {
                    return contentNode.asText();
                } else {
                    return "No advice returned from AI.";
                }
            } else {
                return "Failed with status: " + response.getStatusCode();
            }

        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            return "Failed to generate advice: " + e.getStatusCode();
        } catch (Exception e) {
            e.printStackTrace();
            return "Could not generate advice at this time.";
        }
    }
}
