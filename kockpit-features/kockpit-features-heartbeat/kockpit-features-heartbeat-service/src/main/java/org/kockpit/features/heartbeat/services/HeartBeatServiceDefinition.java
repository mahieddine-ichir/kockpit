package org.kockpit.features.heartbeat.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.kockpit.core.sdk.ServiceDefinition;

@RequiredArgsConstructor
public class HeartBeatServiceDefinition implements ServiceDefinition {

    public static final String SERVICE_NAME = "HeartBeat";

    @Getter
    private final boolean pollingEnabled;

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    public String audience() {
        return null;
    }
}
