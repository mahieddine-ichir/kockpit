package org.kockpit.features.dynaconfig.service;

import org.kockpit.sdk.SdkApplicationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Objects;

@AutoConfiguration
public class DynaConfigServiceAutoConfiguration {

    @Bean
    DynaConfigServiceDefinition dynaConfigServiceDefinition(
            SdkApplicationProperties sdkApplicationProperties,
            @Value("${kockpit.dyna-config.audience:#{null}}") String audience
    ) {
        if (Objects.isNull(audience)) {
            return new DynaConfigServiceDefinition(sdkApplicationProperties.getAppId());
        } else {
            return new DynaConfigServiceDefinition(audience);
        }
    }
}
