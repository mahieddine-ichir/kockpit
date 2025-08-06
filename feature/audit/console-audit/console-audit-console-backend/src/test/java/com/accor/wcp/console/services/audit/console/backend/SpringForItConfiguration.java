package com.accor.wcp.console.services.audit.console.backend;

import com.accor.wcp.console.services.audit.console.backend.search.ElasticSearchClientWrapper;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SpringForItConfiguration {

  public static ElasticSearchClientWrapper mockElasticSearchClientWrapper;

  @Primary
  @Bean
  ElasticSearchClientWrapper mockElasticSearchClientWrapper() {
    mockElasticSearchClientWrapper = Mockito.mock(ElasticSearchClientWrapper.class);
    return mockElasticSearchClientWrapper;
  }
}
