package org.kockpit.service.featureflipping;

import lombok.RequiredArgsConstructor;
import org.kockpit.service.featureflipping.api.FeatureFlippingDto;

@RequiredArgsConstructor
public class FeatureFlippingEvaluatorService {

    private final FeatureFlippingCache featureFlippingCache;

    public Boolean evaluate(String key) {
        return featureFlippingCache.getFlag(key)
                .map(FeatureFlippingDto::getEnabled)
                .orElse(false);
    }
}
