package org.kockpit.features.dynaconfig.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kockpit.communication.MessageCache;
import org.kockpit.sdk.SdkApplicationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Objects;

@AutoConfiguration
public class DynaConfigServiceAutoConfiguration {

    @Bean
    DynaConfigEvaluatorService dynaConfigEvaluatorService(MessageCache messageCache, DynaConfigServiceDefinition dynaConfigServiceDefinition) {
        return new DynaConfigEvaluatorService(messageCache, new ObjectMapper(), dynaConfigServiceDefinition);
    }

    @Bean
    DynaConfigServiceDefinition dynaConfigServiceDefinition(
            SdkApplicationProperties sdkApplicationProperties,
            @Value("${kockpit.dyna-config.audience:#{null}}") String audience,
            @Value("${kockpit.dyna-config.polling.enabled:false}") boolean pollingEnabled

    ) {
        if (Objects.isNull(audience)) {
            return new DynaConfigServiceDefinition(sdkApplicationProperties.getAppId(), pollingEnabled);
        } else {
            return new DynaConfigServiceDefinition(audience, pollingEnabled);
        }
    }
}
