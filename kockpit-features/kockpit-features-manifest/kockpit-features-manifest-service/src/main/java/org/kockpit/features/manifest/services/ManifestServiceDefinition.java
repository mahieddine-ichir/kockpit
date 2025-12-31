package org.kockpit.features.manifest.services;

import lombok.Getter;
import org.kockpit.core.sdk.ServiceDefinition;

public class ManifestServiceDefinition implements ServiceDefinition {

    public static final String SERVICE_NAME = "Manifest";

    @Getter
    private final boolean enabled;

    public ManifestServiceDefinition(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    public String audience() {
        return null;
    }
}
