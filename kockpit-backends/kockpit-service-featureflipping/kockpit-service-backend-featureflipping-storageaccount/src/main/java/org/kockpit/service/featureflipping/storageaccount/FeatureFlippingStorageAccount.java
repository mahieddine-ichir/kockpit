package org.kockpit.service.featureflipping.storageaccount;

import org.kockpit.service.featureflipping.api.FeatureFlippingDto;
import org.kockpit.service.featureflipping.api.FeatureFlippingHistory;
import org.kockpit.service.featureflipping.api.FeatureFlippingService;

import java.util.List;

public class FeatureFlippingStorageAccount implements FeatureFlippingService {

    @Override
    public FeatureFlippingDto update(String domain, String env, FeatureFlippingDto featureFlippingDto) {
        return null;
    }

    @Override
    public List<FeatureFlippingHistory> getHistory(String domain, String env) {
        return List.of();
    }
}
