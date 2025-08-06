package com.accor.wcp.sdk.application.service.cache;

import static java.util.Objects.nonNull;

import com.accor.wcp.sdk.application.communication.App2WCPConsoleCommunicationService;
import javax.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class ApplicationCacheServiceAutoConfiguration {

  @Bean
  CacheApplicationServiceIntegration cacheApplicationServiceIntegration(
      App2WCPConsoleCommunicationService app2WCPConsoleCommunicationService,
      @Autowired(required = false) CacheManager cacheManager) {
    if (nonNull(cacheManager)) {
      return new CacheApplicationServiceIntegration(
          app2WCPConsoleCommunicationService,
          new JSR107CacheHandler(cacheManager, app2WCPConsoleCommunicationService));
    }
    return null;
  }
}
