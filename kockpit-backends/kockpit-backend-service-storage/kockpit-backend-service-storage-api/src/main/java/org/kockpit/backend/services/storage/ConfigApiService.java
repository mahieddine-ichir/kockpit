package org.kockpit.backend.services.storage;

import java.util.List;

public interface ConfigApiService {

    List<Manifest> getConfig();

    ConfigItem save(ConfigItem configItem);

    Object getFeatureFlipping(String domain, String env);
    Object updateFeatureFlag(String domain, String env, String key, Object value);

    List<FeatureFlippingHistory> getHistory(String domain, String env);
}
