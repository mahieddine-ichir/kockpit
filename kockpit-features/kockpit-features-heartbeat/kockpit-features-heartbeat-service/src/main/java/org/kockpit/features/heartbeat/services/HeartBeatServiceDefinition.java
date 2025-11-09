package org.kockpit.features.heartbeat.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.kockpit.core.sdk.ServiceDefinition;

@RequiredArgsConstructor
public class HeartBeatServiceDefinition implements ServiceDefinition {

    @Getter
    private final boolean pollingEnabled;

    @Override
    public String name() {
        return "HeartBeat";
    }

    @Override
    public String audience() {
        return null;
    }
}
