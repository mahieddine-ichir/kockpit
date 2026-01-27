package org.kockpit.audit.httpexchange;

import lombok.RequiredArgsConstructor;
import org.kockpit.audit.api.AuditorEventService;
import org.kockpit.audit.httpexchange.obfuscator.AuditObfuscator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RequiredArgsConstructor
public class AuditRestTemplateBeanPostProcessor implements BeanPostProcessor {

  private final AuditorEventService auditorEvents;
  private final List<AuditObfuscator> bodyAuditObfuscators;
  private final boolean filterAuthorizationHeader;

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof RestTemplate rt) {
      new RestTemplateInterceptorInjector(interceptor(auditorEvents, bodyAuditObfuscators))
          .inject(rt);
    }
    return bean;
  }

  private AuditClientHttpRequestInterceptor interceptor(
      AuditorEventService auditorEvents, List<AuditObfuscator> bodyAuditObfuscators) {
    return new AuditClientHttpRequestInterceptor(auditorEvents, bodyAuditObfuscators, filterAuthorizationHeader);
  }
}
