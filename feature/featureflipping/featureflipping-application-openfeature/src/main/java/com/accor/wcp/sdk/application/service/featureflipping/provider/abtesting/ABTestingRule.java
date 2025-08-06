package com.accor.wcp.sdk.application.service.featureflipping.provider.abtesting;

import java.util.Map;

public interface ABTestingRule {

    String id();

    default boolean activate(Map<String, String> configs) {
        return false;
    }
}
