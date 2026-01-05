package org.kockpit.sample.api.featureflipping;

import lombok.RequiredArgsConstructor;
import org.kockpit.features.featureflipping.service.FeatureFlippingEvaluatorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/feature-flipping")
@RequiredArgsConstructor
public class FeatureFlippingApi {

    private final FeatureFlippingEvaluatorService featureFlippingEvaluatorService;

    private final FeatureFlagService featureFlagService;

    @GetMapping("keys/{key}")
    Map<String, Object> evaluate(@PathVariable String key) {
        return Map.of(key, featureFlippingEvaluatorService.evaluate(key));
    }

    @GetMapping("keys")
    Map<String, Object> keys() {
        return Map.of("compression.enabled", featureFlagService.isCompressionEnabled());
    }
}
