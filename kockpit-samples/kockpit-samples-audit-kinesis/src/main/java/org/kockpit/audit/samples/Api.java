package org.kockpit.audit.samples;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class Api {

    @GetMapping
    public Map<String, ?> sayHello() {
        return Map.of("status", "ok");
    }
}
