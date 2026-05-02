package org.kockpit.core.sdk;

public interface ServiceDefinition {

    String name();

    default String audience() {
        return null;
    }

    default boolean isPollingEnabled() {
        return true;
    }
}
