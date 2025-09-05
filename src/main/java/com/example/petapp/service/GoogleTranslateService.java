package com.example.petapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;

@Service
public class GoogleTranslateService {

    @Value("${google.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.create("https://translation.googleapis.com");

    public Mono<String> translate(String text, String targetLang) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/language/translate/v2")
                        .queryParam("key", apiKey)
                        .build())
                .bodyValue(Map.of("q", text, "target", targetLang))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    var data = (Map<String, Object>) response.get("data");
                    var translations = (java.util.List<Map<String, String>>) data.get("translations");
                    return translations.get(0).get("translatedText");
                });
    }
}

