package com.accor.wcp.sdk.application.service.featureflipping;

import com.accor.wcp.sdk.application.communication.App2WCPConsoleCommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.Objects.isNull;

@Configuration
class ApplicationFeatureFlippingServiceAutoConfiguration {
    @Bean
    FeatureFlippingKeysServiceImpl featureFlippingPropertiesService() {
        return new FeatureFlippingKeysServiceImpl();
    }

  @Bean
  FeatureFlippingApplicationServiceIntegration featureFlippingApplicationServiceIntegration(
      App2WCPConsoleCommunicationService app2WCPConsoleCommunicationService,
      FeatureFlippingKeysServiceImpl featureFlippingPropertiesService,
      @Autowired(required = false) FeatureFlippingKeyUpdateHandler featureFlippingKeyUpdateHandler) {

    if (isNull(featureFlippingKeyUpdateHandler)) {
        featureFlippingKeyUpdateHandler = new DefaultFeatureFlippingKeyUpdateHandler();
    }
    return new FeatureFlippingApplicationServiceIntegration(app2WCPConsoleCommunicationService,
            featureFlippingKeyUpdateHandler,
            featureFlippingPropertiesService);
    }
}
