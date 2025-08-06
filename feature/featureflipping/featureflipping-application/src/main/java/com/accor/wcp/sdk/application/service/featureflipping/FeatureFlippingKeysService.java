package com.accor.wcp.sdk.application.service.featureflipping;

import java.util.Map;
import java.util.Optional;

public interface FeatureFlippingKeysService {
    Optional<Boolean> getBoolean(String key);

    Optional<String> getString(String key);

    Optional<Integer> getInteger(String key);

    // FIXME - CJO - review (only for POC A/B Testing)
    Optional<Map<String, String>> getEvaluationConfig(String key);
}
