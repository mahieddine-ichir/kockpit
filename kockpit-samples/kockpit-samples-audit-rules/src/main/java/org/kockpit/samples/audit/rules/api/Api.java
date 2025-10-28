package org.kockpit.samples.audit.rules.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/rules")
@RequiredArgsConstructor
public class Api {

    private final MyFlow myFlow;

    @GetMapping
    Map<String, Object> rules() {
        return Map.of("status", myFlow.status());
    }
}
