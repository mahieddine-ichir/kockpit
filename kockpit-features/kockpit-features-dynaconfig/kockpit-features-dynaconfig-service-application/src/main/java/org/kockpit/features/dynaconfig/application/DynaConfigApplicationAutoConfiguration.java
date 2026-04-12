package org.kockpit.features.dynaconfig.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.Nullable;

@AutoConfiguration
@ConditionalOnProperty(
        value = "kockpit.dyna-config.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class DynaConfigApplicationAutoConfiguration {

    @ConditionalOnProperty(
            value = "kockpit.dyna-config.post-processor.enabled",
            havingValue = "true"
    )
    @Bean
    DynaConfigBeanProcessor dynaConfigBeanProcessor() {
        return new DynaConfigBeanProcessor();
    }

    @ConditionalOnBean(DynaConfigBeanProcessor.class)
    @Bean
    DynaConfigMessageListener dynaConfigMessageListener(
            DynaConfigBeanProcessor beanProcessor,
            ObjectMapper objectMapper,
            @Autowired(required = false) @Nullable ConversionService conversionService) {
        return new DynaConfigMessageListener(beanProcessor, objectMapper, conversionService);
    }
}
