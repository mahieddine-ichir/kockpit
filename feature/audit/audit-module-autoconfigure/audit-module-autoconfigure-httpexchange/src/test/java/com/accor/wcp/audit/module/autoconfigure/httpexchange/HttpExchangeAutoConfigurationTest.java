package com.accor.wcp.audit.module.autoconfigure.httpexchange;

import static org.assertj.core.api.Assertions.assertThat;

import com.accor.wcp.audit.AuditorEventService;
import com.accor.wcp.audit.module.httpexchange.obfuscator.AuditObfuscator;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HttpExchangeAutoConfigurationTest {

  @Mock AuditorEventService wcpAuditor;

  List<AuditObfuscator> auditObfuscators = Collections.emptyList();

  @Test
  void shouldInstantiateAnAuditClientHttpRequestInterceptor() {
    HttpExchangeAutoConfiguration httpExchangeAutoConfiguration =
        new HttpExchangeAutoConfiguration();
    assertThat(
            httpExchangeAutoConfiguration.auditClientHttpRequestInterceptor(
                wcpAuditor, auditObfuscators))
        .isNotNull();
  }

  @Test
  void shouldInstantiateAnAuditRestTemplateBeanPostProcessor() {
    HttpExchangeAutoConfiguration httpExchangeAutoConfiguration =
        new HttpExchangeAutoConfiguration();
    assertThat(
            httpExchangeAutoConfiguration.auditRestTemplateBeanPostProcessor(
                wcpAuditor, auditObfuscators))
        .isNotNull();
  }

  @Test
  void shouldInstantiateARestTemplateCustomizer() {
    HttpExchangeAutoConfiguration httpExchangeAutoConfiguration =
        new HttpExchangeAutoConfiguration();
    assertThat(
            httpExchangeAutoConfiguration.auditRestTemplateCustomizer(wcpAuditor, auditObfuscators))
        .isNotNull();
  }
}
