package org.kockpit.features.dynaconfig.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.kockpit.core.sdk.ServiceDefinition;

@RequiredArgsConstructor
public class DynaConfigServiceDefinition implements ServiceDefinition {

    public static final String DYNA_CONFIG = "dyna-config";

    @Getter
    private final String audience;

    @Getter
    private final boolean pollingEnabled;

    @Override
    public String name() {
        return DYNA_CONFIG;
    }

    @Override
    public String audience() {
        return audience;
    }
}
