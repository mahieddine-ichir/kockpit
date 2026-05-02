package org.kockpit.features.featureflipping.service;

import org.kockpit.sdk.SdkApplicationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.Objects;

@AutoConfiguration
@ConditionalOnProperty(
        value = "kockpit.feature-flipping.enabled",
        havingValue = "true"
)
public class FeatureFlippingServiceAutoConfiguration {

    @Bean
    FeatureFlippingServiceDefinition featureFlippingServiceDefinition(
            SdkApplicationProperties sdkApplicationProperties,
            @Value("${kockpit.feature-flipping.audience:#{null}}") String audience,
            @Value("${kockpit.feature-flipping.polling.enabled:false}") boolean pollingEnabled

    ) {
        if (Objects.isNull(audience)) {
            return new FeatureFlippingServiceDefinition(sdkApplicationProperties.getAppId());
        } else {
            return new FeatureFlippingServiceDefinition(audience);
        }
    }
}
