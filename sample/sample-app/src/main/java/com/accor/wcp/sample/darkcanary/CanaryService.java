package com.accor.wcp.sample.darkcanary;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CanaryService {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    Map<String, Object> offers(String version) {
        if (version.equalsIgnoreCase("v2")) {
            return objectMapper.readValue(this.getClass().getResourceAsStream("/darkcanarytesting/offers_v2_d39RXKdHPXnpsLZ3cu.json"), Map.class);
        } else {
            return objectMapper.readValue(this.getClass().getResourceAsStream("/darkcanarytesting/offers_v1_d39RXKdHPXnpsLZ3cu.json"), Map.class);
        }
    }
}
