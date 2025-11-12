package org.kockpit.audit.openapi;

import lombok.RequiredArgsConstructor;
import org.kockpit.audit.api.AuditorKeyValueService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
class OperationIdSetterInterceptorConfig implements WebMvcConfigurer {

    private final AuditorKeyValueService auditorKeyValueService;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new OperationIdSetterInterceptor(auditorKeyValueService));
  }
}
