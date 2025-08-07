package com.accor.wcp.web.rest.config.localresolver.integrationtest;

import com.accor.wcp.web.rest.config.localresolver.LocalesReferential;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
class TestLocalResolverConfigurationWithCustomLocalReferential {

  @Bean
  LocalesReferential localesReferential() {

    return new LocalesReferential() {

      @Override
      public List<Locale> getSupportedLocales() {
        return List.of(Locale.GERMAN, Locale.CANADA);
      }

      @Override
      public Locale getDefaultLocale() {
        return Locale.CANADA;
      }
    };
  }
}
