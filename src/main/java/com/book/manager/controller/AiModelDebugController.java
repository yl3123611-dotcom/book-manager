package com.book.manager.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@RestController
public class AiModelDebugController {

    @Value("${langchain4j.open-ai.embedding-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.embedding-model.api-key}")
    private String apiKey;

    @GetMapping("/debug/embedding-models")
    public String listEmbeddingModels() {
        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // OpenAI兼容：GET /v1/models
        ResponseEntity<String> resp = rt.exchange(baseUrl + "/models", HttpMethod.GET, entity, String.class);
        return resp.getBody();
    }
}
