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
class AuditRestTemplateBeanPostProcessorTest {

  @Mock AuditorEventService wcpAuditor;

  List<AuditObfuscator> auditObfuscators = Collections.emptyList();

  @Test
  void shouldCustomizeRestTemplateWithAuditClientHttpRequestInterceptor() {

    AuditRestTemplateBeanPostProcessor auditRestTemplateBeanPostProcessor =
        new AuditRestTemplateBeanPostProcessor(wcpAuditor, auditObfuscators, true);
    RestTemplate restTemplate =
        new RestTemplate(
            new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
    auditRestTemplateBeanPostProcessor.postProcessAfterInitialization(restTemplate, "");
    assertThat(restTemplate.getInterceptors())
        .hasAtLeastOneElementOfType(AuditClientHttpRequestInterceptor.class);
  }
}
