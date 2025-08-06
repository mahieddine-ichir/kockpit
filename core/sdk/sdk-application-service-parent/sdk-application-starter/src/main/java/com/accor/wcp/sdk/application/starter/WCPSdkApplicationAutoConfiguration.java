package com.accor.wcp.sdk.application.starter;

import static java.util.Objects.nonNull;

import com.accor.wcp.sdk.application.config.ApplicationSdkConfigurationPropertiesConfig;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** SDK Properties bootstrap configuration class. */
@Slf4j
@Configuration
@ConditionalOnProperty(name = {"wcp.sdk.env", "wcp.sdk.domain", "wcp.sdk.application.id"})
@ComponentScan({
  "com.accor.wcp.sdk.application.starter",
  "com.accor.wcp.sdk.application.communication.impl"
})
@Import(ApplicationSdkConfigurationPropertiesConfig.class)
class WCPSdkApplicationAutoConfiguration {
  @Bean
  DefaultSdkApplicationProperties sdkApplicationProperties(
      @Value("${wcp.sdk.domain}") String domain,
      @Value("${wcp.sdk.env}") String wcpEnv,
      @Value("${wcp.sdk.application.id}") String applicationId,
      @Value("${wcp.sdk.application.env}") String applicationEnv,
      @Value("${wcp.sdk.communication.enabled:true}") boolean communicationEnabled,
      @Value("${wcp.sdk.initialization.timeout:PT15S}") Duration initializationTimeout,
      @Autowired(required = false) BuildProperties buildProperties) {
    String applicationVersion;
    if (nonNull(buildProperties)) {
      applicationVersion = buildProperties.getVersion();
    } else {
      applicationVersion = "undefined";
    }

    // Special case for wcpEnv "none"
    if ("none".equals(wcpEnv)) {
      log.info("WCP Env 'none' cause communication enabled = false");
      communicationEnabled = false;
    }

    DefaultSdkApplicationProperties sdkApplicationProperties =
        DefaultSdkApplicationProperties.builder()
            .wcpEnv(wcpEnv)
            .domain(domain)
            .applicationId(applicationId)
            .applicationEnv(applicationEnv)
            .applicationVersion(applicationVersion)
            .communicationEnabled(communicationEnabled)
            .initializationTimeout(initializationTimeout)
            .build();
    log.info("Using sdk application configuration : {}", sdkApplicationProperties);
    return sdkApplicationProperties;
  }
}
