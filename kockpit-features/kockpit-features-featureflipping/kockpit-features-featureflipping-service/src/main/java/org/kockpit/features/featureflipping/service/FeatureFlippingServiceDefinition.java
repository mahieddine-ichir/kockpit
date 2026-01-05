package org.kockpit.features.featureflipping.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.kockpit.core.sdk.ServiceDefinition;

@RequiredArgsConstructor
public class FeatureFlippingServiceDefinition implements ServiceDefinition {

    public static final String FEATURE_FLIPPING = "feature-flipping";

    @Getter
    private final String audience;

    @Getter
    private final boolean pollingEnabled;

    @Override
    public String name() {
        return FEATURE_FLIPPING;
    }

    @Override
    public String audience() {
        return audience;
    }
}
