package org.kockpit.features.featureflipping.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.kockpit.core.sdk.ServiceDefinition;

@RequiredArgsConstructor
public class FeatureFlippingServiceDefinition implements ServiceDefinition {

    @Getter
    private final String audience;

    @Getter
    private final boolean pollingEnabled;

    @Override
    public String name() {
        return "FeatureFlipping";
    }

    @Override
    public String audience() {
        return audience;
    }
}
