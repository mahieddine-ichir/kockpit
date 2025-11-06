package org.kockpit.core.sdk;

public interface ServiceDefinition {

    String name();

    String audience();

    default boolean isPollingEnabled() {
        return true;
    }
}
