package com.accor.wcp.audit.module.httpexchange;

import com.accor.wcp.audit.AuditorEventService;
import com.accor.wcp.audit.module.httpexchange.obfuscator.AuditObfuscator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AuditRestTemplateCustomizerTest {

  @Mock AuditorEventService wcpAuditor;

  List<AuditObfuscator> auditObfuscators = Collections.emptyList();

  @Test
  void shouldCustomizeRestTemplateWithAuditClientHttpRequestInterceptor() {
    AuditClientHttpRequestInterceptor auditClientHttpRequestInterceptor =
        new AuditClientHttpRequestInterceptor(wcpAuditor, auditObfuscators, true);
    AuditRestTemplateCustomizer auditRestTemplateCustomizer =
        new AuditRestTemplateCustomizer(auditClientHttpRequestInterceptor);
    RestTemplate restTemplate = createRestTemplate();
    auditRestTemplateCustomizer.customize(restTemplate);
    assertThat(restTemplate.getInterceptors()).containsOnlyOnce(auditClientHttpRequestInterceptor);
  }

  @Test
  void shouldCustomizeRestTemplateWithOnlyOneAuditClientHttpRequestInterceptor() {
    AuditClientHttpRequestInterceptor auditClientHttpRequestInterceptor =
        new AuditClientHttpRequestInterceptor(wcpAuditor, auditObfuscators, true);
    AuditRestTemplateCustomizer auditRestTemplateCustomizer =
        new AuditRestTemplateCustomizer(auditClientHttpRequestInterceptor);
    RestTemplate restTemplate = createRestTemplate();
    auditRestTemplateCustomizer.customize(restTemplate);
    auditRestTemplateCustomizer.customize(restTemplate);
    assertThat(restTemplate.getInterceptors()).containsOnlyOnce(auditClientHttpRequestInterceptor);
  }

  private RestTemplate createRestTemplate() {
    return new RestTemplate(
        new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
  }
}
