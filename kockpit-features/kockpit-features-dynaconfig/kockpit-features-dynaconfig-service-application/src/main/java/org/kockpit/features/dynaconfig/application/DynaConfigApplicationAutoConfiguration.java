package org.kockpit.features.dynaconfig.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.Nullable;

@AutoConfiguration
@ConditionalOnProperty(
        value = "kockpit.dyna-config.application.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class DynaConfigApplicationAutoConfiguration {

    @Bean
    DynaConfigBeanProcessor dynaConfigBeanProcessor() {
        return new DynaConfigBeanProcessor();
    }

    @Bean
    DynaConfigMessageListener dynaConfigMessageListener(
            DynaConfigBeanProcessor beanProcessor,
            ObjectMapper objectMapper,
            @Autowired(required = false) @Nullable ConversionService conversionService) {
        return new DynaConfigMessageListener(beanProcessor, objectMapper, conversionService);
    }
}