package com.accor.wcp.audit.module.httpexchange;

import com.accor.wcp.audit.AuditorEventService;
import com.accor.wcp.audit.module.httpexchange.obfuscator.AuditObfuscator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class AuditRestTemplateBeanPostProcessor implements BeanPostProcessor {

  private final AuditorEventService auditorEvents;
  private final List<AuditObfuscator> bodyAuditObfuscators;
  private final boolean filterAuthorizationHeader;

  public AuditRestTemplateBeanPostProcessor(
      AuditorEventService auditorEvents, List<AuditObfuscator> bodyAuditObfuscators, boolean filterAuthorizationHeader) {
    this.auditorEvents = auditorEvents;
    this.bodyAuditObfuscators = bodyAuditObfuscators;
    this.filterAuthorizationHeader = filterAuthorizationHeader;
  }

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
