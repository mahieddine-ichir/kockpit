package org.kockpit.audit.sampleapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("sayHello")
public class Api {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("{name}")
    Map<?,?> sayHello(@PathVariable String name) {
        return restTemplate.getForObject("http://localhost:8080/sampleapp/sayHello/v2/" + name, Map.class);
    }

    @GetMapping("/v2/{name}")
    Map<String, String> sayHelloV2(@PathVariable String name) {
        return Map.of("hello-v2", name);
    }
}
