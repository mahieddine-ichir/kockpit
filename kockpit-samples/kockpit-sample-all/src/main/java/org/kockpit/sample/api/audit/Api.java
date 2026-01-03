package org.kockpit.sample.api.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class Api {

    private static final String API_URL = "https://randomuser.me/api/";
    private static final String FACTS_API_URL = "https://catfact.ninja";

    private final RestTemplate restTemplate;

    @GetMapping("facts")
    public CatFact facts() {
        return restTemplate.getForObject(FACTS_API_URL + "/fact", CatFact.class);
    }

    @GetMapping("sayHello/{name}")
    RandomUserResponse sayHello(
            @PathVariable String name
    ) {
        return restTemplate.getForObject(API_URL, RandomUserResponse.class);
    }

    @PostMapping("createMessage")
    ResponseEntity<Map<String, Object>> createMessage(
            @RequestBody Map<String, Object> messageData
    ) {
        if (Math.random() < 0.25) {
            throw new RuntimeException("Creation failed!");
        } else if (Math.random() < 0.5) {
            throw new IllegalArgumentException("Body invalid!");
        }
        return ResponseEntity.created(URI.create("http://localhost:8081/sample-app/sayHello")).body(
                Map.of(
                "status", "created",
                "id", System.currentTimeMillis(),
                "message", messageData.get("message") != null ? messageData.get("message") : "none",
                "timestamp", Instant.now().toString()
                ));
    }

    @DeleteMapping("sayHello")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteMessage() {
        if (Math.random() < 0.25) {
            throw new RuntimeException("Delete failed!");
        }
    }
}
