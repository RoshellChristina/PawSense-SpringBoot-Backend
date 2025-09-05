package com.example.petapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {
    private final RestTemplate restTemplate;
    private final String embedUrl;

    @Autowired
    public EmbeddingService(RestTemplate restTemplate, @Value("${embedding.service.url}") String embedUrl) {
        this.restTemplate = restTemplate;
        this.embedUrl = embedUrl;
    }

    /**
     * Returns a single Float[] embedding for the provided text (combined fields).
     */
    public Float[] getTextEmbedding(String text) {
        try {
            Map<String, String> req = Map.of("text", text == null ? "" : text);
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restTemplate.postForObject(embedUrl, req, Map.class);
            if (resp == null || !resp.containsKey("embedding")) return null;

            @SuppressWarnings("unchecked")
            List<Number> arr = (List<Number>) resp.get("embedding");
            Float[] emb = new Float[arr.size()];
            for (int i = 0; i < arr.size(); i++) emb[i] = arr.get(i).floatValue();
            return emb;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Optional: request multiple embeddings in one call
     */
    @SuppressWarnings("unchecked")
    public Float[][] getEmbeddingsBatch(List<String> texts) {
        try {
            Map<String, Object> req = Map.of("texts", texts);
            Map<String, Object> resp = restTemplate.postForObject(embedUrl, req, Map.class);
            if (resp == null || !resp.containsKey("embeddings")) return null;
            List<List<Number>> arrs = (List<List<Number>>) resp.get("embeddings");
            Float[][] out = new Float[arrs.size()][];
            for (int i = 0; i < arrs.size(); i++) {
                List<Number> row = arrs.get(i);
                Float[] rowF = new Float[row.size()];
                for (int j = 0; j < row.size(); j++) rowF[j] = row.get(j).floatValue();
                out[i] = rowF;
            }
            return out;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
