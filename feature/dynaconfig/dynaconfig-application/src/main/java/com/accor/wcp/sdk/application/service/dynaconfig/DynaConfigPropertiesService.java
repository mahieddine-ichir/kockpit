package com.accor.wcp.sdk.application.service.dynaconfig;

import java.util.Optional;

public interface DynaConfigPropertiesService {
    Optional<Boolean> getBoolean(String property);

    Optional<String> getString(String property);

    Optional<Integer> getInteger(String property);
}
