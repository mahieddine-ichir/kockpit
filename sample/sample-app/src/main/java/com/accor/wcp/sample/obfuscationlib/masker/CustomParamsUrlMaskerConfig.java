package com.accor.wcp.sample.obfuscationlib.masker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CustomParamsUrlMaskerConfig {

    @Bean
    @ConditionalOnMissingBean
    CustomParamsUrlMasker paramsUrlMasker() {
        return new CustomParamsUrlMasker();
    }
}
