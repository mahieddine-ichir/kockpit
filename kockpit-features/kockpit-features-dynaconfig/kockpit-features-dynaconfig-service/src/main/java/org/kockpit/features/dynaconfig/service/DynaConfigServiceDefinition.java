package org.kockpit.features.dynaconfig.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.kockpit.core.sdk.ServiceDefinition;

@RequiredArgsConstructor
public class DynaConfigServiceDefinition implements ServiceDefinition {

    @Getter
    private final String audience;

    @Getter
    private final boolean pollingEnabled;

    @Override
    public String name() {
        return "DynaConfig";
    }

    @Override
    public String audience() {
        return audience;
    }
}
