package org.kockpit.service.featureflipping.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/{domain}/{env}/feature-flipping")
@RequiredArgsConstructor
@Slf4j
public class FeatureFlippingApi {

    private final FeatureFlippingService featureFlippingService;

    @PutMapping
    ResponseEntity<FeatureFlippingDto> update(
            @PathVariable String domain,
            @PathVariable String env,
            @RequestParam String key,
            @RequestBody FeatureFlippingDto featureFlippingDto
    ) {
        log.debug("Feature Flipping {} enabled", key);
        return ResponseEntity.ok(featureFlippingService.update(domain, env, featureFlippingDto));
    }

    @GetMapping("history")
    ResponseEntity<List<FeatureFlippingHistory>> getHistory(
            @PathVariable String domain,
            @PathVariable String env
    ) {
        return ResponseEntity.ok(featureFlippingService.getHistory(domain, env));
    }

}
