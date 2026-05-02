package org.kockpit.features.heartbeat.services;

import lombok.RequiredArgsConstructor;
import org.kockpit.core.sdk.ServiceDefinition;

@RequiredArgsConstructor
public class HeartBeatServiceDefinition implements ServiceDefinition {

    public static final String SERVICE_NAME = "heartbeat";

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    public boolean isPollingEnabled() {
        return false;
    }
}
