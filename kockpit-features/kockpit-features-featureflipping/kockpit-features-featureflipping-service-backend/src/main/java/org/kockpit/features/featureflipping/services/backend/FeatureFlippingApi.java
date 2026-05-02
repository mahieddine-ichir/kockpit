package org.kockpit.features.featureflipping.services.backend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.features.featureflipping.service.FeatureFlippingDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/{domain}/{env}/feature-flipping/{id}")
@RequiredArgsConstructor
@Slf4j
public class FeatureFlippingApi {

    private final FeatureFlippingService featureFlippingService;

    @GetMapping
    ResponseEntity<List<FeatureFlippingDto>> get(
            @PathVariable String domain,
            @PathVariable String env,
            @PathVariable String id
    ) {
        return ResponseEntity.ok(featureFlippingService.get(domain, env, id));
    }

    @PutMapping
    ResponseEntity<FeatureFlippingDto> update(
            @PathVariable String domain,
            @PathVariable String env,
            @PathVariable String id,
            @RequestParam String key,
            @RequestBody FeatureFlippingDto featureFlippingDto
    ) {
        log.debug("Feature Flipping {} enabled?, {} (domain {}, env {})", key, featureFlippingDto.getEnabled(), domain, env);
        return ResponseEntity.ok(featureFlippingService.update(domain, env, id, featureFlippingDto));
    }

    @PostMapping("_sync")
    ResponseEntity<Void> sync(
            @PathVariable String domain,
            @PathVariable String env,
            @PathVariable String id
    ) {
        featureFlippingService.republish();
        return ResponseEntity.ok().build();
    }

    @GetMapping("history")
    ResponseEntity<List<FeatureFlippingDto>> history(
            @PathVariable String domain,
            @PathVariable String env,
            @PathVariable String id
            ) {
        return ResponseEntity.ok(featureFlippingService.getHistory(domain, env));
    }

}
