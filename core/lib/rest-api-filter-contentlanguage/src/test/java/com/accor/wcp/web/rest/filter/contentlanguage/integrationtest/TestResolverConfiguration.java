package com.accor.wcp.web.rest.filter.contentlanguage.integrationtest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.LocaleResolver;

@TestConfiguration
class TestResolverConfiguration {

  @Bean
  LocaleResolver localeResolver() {

    return new LocaleResolver() {
      @Override
      public Locale resolveLocale(HttpServletRequest request) {
        return Locale.GERMAN;
      }

      @Override
      public void setLocale(
          HttpServletRequest request, HttpServletResponse response, Locale locale) {}
    };
  }
}
