package org.kockpit.sample.api.audit;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class Api {

    @GetMapping("sayHello/{name}")
    Map<String, Object> sayHello(
            @PathVariable String name
    ) {
        return Map.of("hello", name);
    }

    @PostMapping("createMessage")
    Map<String, Object> createMessage(
            @RequestBody Map<String, Object> messageData
    ) {
        return Map.of(
                "status", "created",
                "id", System.currentTimeMillis(),
                "message", messageData.get("message"),
                "timestamp", java.time.Instant.now().toString()
        );
    }
}
