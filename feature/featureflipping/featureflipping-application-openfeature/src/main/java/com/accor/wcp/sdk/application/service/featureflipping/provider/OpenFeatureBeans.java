package com.accor.wcp.sdk.application.service.featureflipping.provider;

import com.accor.wcp.sdk.application.service.featureflipping.FeatureFlippingKeysService;
import dev.openfeature.sdk.OpenFeatureAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenFeatureBeans {

    @Bean
    OpenFeatureAPI openFeatureAPI(FeatureFlippingKeysService flippingPropertiesService) {
        FeatureFlippingProvider featureFlippingProvider = new FeatureFlippingProvider(flippingPropertiesService);
        final OpenFeatureAPI openFeatureAPI = OpenFeatureAPI.getInstance();
        openFeatureAPI.setProvider(featureFlippingProvider);

        return openFeatureAPI;
    }
}