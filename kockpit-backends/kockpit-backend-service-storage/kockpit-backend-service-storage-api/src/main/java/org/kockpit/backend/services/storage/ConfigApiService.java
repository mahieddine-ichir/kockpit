package org.kockpit.backend.services.storage;

import java.util.List;

public interface ConfigApiService {

    List<Manifest> list();

    ConfigItem save(ConfigItem configItem);
}
