package org.kockpit.service.featureflipping.api;

import java.util.List;

public interface FeatureFlippingService {

    FeatureFlippingDto update(String domain, String env, FeatureFlippingDto featureFlippingDto);

    List<FeatureFlippingHistory> getHistory(String domain, String env);

    List<FeatureFlippingDto> findAll(String  domain, String env);
}
