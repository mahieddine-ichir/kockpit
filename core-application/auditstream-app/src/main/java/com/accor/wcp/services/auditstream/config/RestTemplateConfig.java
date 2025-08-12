package com.accor.wcp.services.auditstream.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Value("${rest-template.connectTimout}")
    private Duration connectTimeout;

    @Value("${rest-template.readTimeout}")
    private Duration readTimeout;

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .build();
    }
}
