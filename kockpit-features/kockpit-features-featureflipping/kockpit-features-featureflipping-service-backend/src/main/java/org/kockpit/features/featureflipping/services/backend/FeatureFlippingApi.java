package org.kockpit.features.featureflipping.services.backend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.features.featureflipping.service.FeatureFlippingDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/{domain}/{env}/{appId}/feature-flipping")
@RequiredArgsConstructor
@Slf4j
public class FeatureFlippingApi {

    private final FeatureFlippingService featureFlippingService;

    @PutMapping
    ResponseEntity<FeatureFlippingDto> update(
            @PathVariable String domain,
            @PathVariable String env,
            @PathVariable String appId,
            @RequestParam String key,
            @RequestBody FeatureFlippingDto featureFlippingDto
    ) {
        log.debug("Feature Flipping {} enabled?, {} (domain {}, env {})", key, featureFlippingDto.getEnabled(), domain, env);
        return ResponseEntity.ok(featureFlippingService.update(domain, env, appId, featureFlippingDto));
    }

    @GetMapping("history")
    ResponseEntity<List<FeatureFlippingDto>> history(
            @PathVariable String domain,
            @PathVariable String env
    ) {
        return ResponseEntity.ok(featureFlippingService.getHistory(domain, env));
    }

}
