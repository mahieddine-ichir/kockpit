package org.kockpit.service.featureflipping;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.service.featureflipping.api.FeatureFlippingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;

@AutoConfiguration
@EnableScheduling
@Slf4j
public class FeatureFlippingAutoConfiguration {

    @ConditionalOnBean(FeatureFlippingService.class)
    @Bean
    FeatureFlippingScheduler featureFlippingClient(
            FeatureFlippingService featureFlippingService,
            FeatureFlippingCache featureFlippingCache,
            TaskScheduler taskScheduler,
            @Value("${kockpit.feature-flipping.client.scheduling:PT10S}") String clientScheduling,
            @Value("${kockpit.sdk.domain}") String domain, // fixme replace with SdkApplicationProperties
            @Value("${kockpit.sdk.env}") String env // fixme replace with SdkApplicationProperties
    ) {
        FeatureFlippingScheduler featureFlippingScheduler = new FeatureFlippingScheduler(
                featureFlippingService,
                featureFlippingCache,
                taskScheduler
        );
        featureFlippingScheduler.start(domain, env, Duration.parse(clientScheduling));
        return featureFlippingScheduler;
    }

    @Bean
    FeatureFlippingEvaluatorService featureFlippingEvaluatorService(FeatureFlippingCache featureFlippingCache) {
        return new FeatureFlippingEvaluatorService(featureFlippingCache);
    }

    @Bean
    FeatureFlippingCache featureFlippingCache() {
        log.info("Feature-Flipping, use inMemory cache");
        return new FeatureFlippingInMemoryCache();
    }
}
