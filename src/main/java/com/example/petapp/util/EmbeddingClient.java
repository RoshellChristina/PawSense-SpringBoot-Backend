package com.example.petapp.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Small wrapper around your Python embed server.
 * Exposes getEmbedding(String) -> normalized Double[] (boxed for JDBC)
 */
@Component
public class EmbeddingClient {

    private final RestTemplate rest;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String EMBED_URL = "http://localhost:6000/embed"; // adjust if needed

    @Autowired
    public EmbeddingClient(RestTemplate rest) {
        this.rest = rest;
    }

    /**
     * Public method used by services. Returns normalized Double[] or throws RuntimeException on failure.
     */
    public Double[] getEmbedding(String text) {
        try {
            if (text == null) text = "";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> payload = Map.of("text", text);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> resp = rest.postForEntity(EMBED_URL, entity, String.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw new RuntimeException("Embed server error: " + resp.getStatusCode());
            }

            JsonNode root = mapper.readTree(resp.getBody());
            JsonNode embNode = root.get("embedding");
            if (embNode == null || !embNode.isArray()) {
                throw new RuntimeException("Unexpected embed response: " + resp.getBody());
            }

            int n = embNode.size();
            double norm = 0.0;
            Double[] out = new Double[n];
            for (int i = 0; i < n; i++) {
                double v = embNode.get(i).asDouble();
                out[i] = v;
                norm += v * v;
            }
            norm = Math.sqrt(norm);
            if (norm > 0.0) {
                for (int i = 0; i < n; i++) out[i] = out[i] / norm;
            }
            return out;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to obtain embedding", ex);
        }
    }
}
