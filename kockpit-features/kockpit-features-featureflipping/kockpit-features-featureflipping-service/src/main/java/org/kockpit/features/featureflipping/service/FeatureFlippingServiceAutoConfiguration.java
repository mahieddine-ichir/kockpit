package org.kockpit.features.featureflipping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kockpit.communication.MessageCache;
import org.kockpit.sdk.SdkApplicationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Objects;

@AutoConfiguration
public class FeatureFlippingServiceAutoConfiguration {

    @Bean
    FeatureFlippingEvaluatorService evaluate(MessageCache messageCache, FeatureFlippingServiceDefinition featureFlippingServiceDefinition) {
        return new FeatureFlippingEvaluatorService(messageCache, new ObjectMapper(), featureFlippingServiceDefinition);
    }

    @Bean
    FeatureFlippingServiceDefinition featureFlippingServiceDefinition(
            SdkApplicationProperties sdkApplicationProperties,
            @Value("${kockpit.sdk.featureflipping.audience:#{null}}") String audience,
            @Value("${kockpit.sdk.featureflipping.polling.enabled:true}") boolean pollingEnabled

    ) {
        if (Objects.isNull(audience)) {
            return new FeatureFlippingServiceDefinition(sdkApplicationProperties.getAppId(), pollingEnabled);
        } else {
            return new FeatureFlippingServiceDefinition(audience, pollingEnabled);
        }
    }
}
