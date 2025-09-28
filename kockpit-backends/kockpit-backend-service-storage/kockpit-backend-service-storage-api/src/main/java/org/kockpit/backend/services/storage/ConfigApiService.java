package org.kockpit.backend.services.storage;

import java.util.List;

public interface ConfigApiService {

    List<ConfigItem> getConfig();

    ConfigItem save(ConfigItem configItem);
}
