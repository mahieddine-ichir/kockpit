package org.kockpit.service.featureflipping;

import org.kockpit.service.featureflipping.api.FeatureFlippingDto;

import java.util.Optional;

public interface FeatureFlippingCache {

    void add(FeatureFlippingDto featureFlippingDto);

    Optional<FeatureFlippingDto> getFlag(String key);
}
