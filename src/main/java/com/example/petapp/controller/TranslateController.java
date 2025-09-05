package com.example.petapp.controller;

import com.example.petapp.service.GoogleTranslateService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/translate")
public class TranslateController {

    private final GoogleTranslateService translateService;

    public TranslateController(GoogleTranslateService translateService) {
        this.translateService = translateService;
    }

    @PostMapping
    public Mono<Map<String, String>> translate(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        String target = body.getOrDefault("target", "si");
        return translateService.translate(text, target)
                .map(translated -> Map.of("translatedText", translated));
    }
}
