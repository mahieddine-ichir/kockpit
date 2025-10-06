package org.kockpit.backend.services.storage;

import java.util.List;

public interface ConfigApiService {

    List<Manifest> getConfig();

    ConfigItem save(ConfigItem configItem);
}
