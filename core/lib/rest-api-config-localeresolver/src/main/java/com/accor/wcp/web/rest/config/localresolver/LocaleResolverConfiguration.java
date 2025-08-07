package com.accor.wcp.web.rest.config.localresolver;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.LocaleResolver;

@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
class LocaleResolverConfiguration {

  @Bean
  @Primary
  public LocaleResolver localeResolver(LocalesReferential localesReferential) {
    ReferentialAcceptHeaderLocaleResolver localeResolver =
        new ReferentialAcceptHeaderLocaleResolver();
    localeResolver.setSupportedLocales(localesReferential.getSupportedLocales());
    localeResolver.setDefaultLocale(localesReferential.getDefaultLocale());

    return localeResolver;
  }

  @Bean
  @ConditionalOnMissingBean
  LocalesReferential defaultLocalesReferential() {
    return new DefaultLocalesReferential();
  }
}
