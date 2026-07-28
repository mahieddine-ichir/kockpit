package org.kockpit.audit.openapi;

import org.springframework.boot.autoconfigure.AutoConfiguration;

import lombok.RequiredArgsConstructor;
import org.kockpit.audit.api.AuditorKeyValueService;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@RequiredArgsConstructor
class OperationIdSetterInterceptorConfig implements WebMvcConfigurer {

    private final AuditorKeyValueService auditorKeyValueService;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new OperationIdSetterInterceptor(auditorKeyValueService));
  }
}
