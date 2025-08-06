package com.accor.wcp.audit.notification.autoconfigure.http;

import com.accor.wcp.audit.notification.http.HttpAuditReportNotificationService;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class HttpAuditNotificationServiceConfiguration {

  private final String auditNotificationUrl;

  RestTemplate auditRestemplate() {
    RestTemplate restTemplate = new RestTemplate();
    // FIXME - why must we put explicitly converters? (if no => send XML !)
    restTemplate.setMessageConverters(
        Collections.singletonList(new MappingJackson2HttpMessageConverter()));
    return restTemplate;
  }

  @Bean
  public HttpAuditReportNotificationService httpAuditNotificationService() {
    return new HttpAuditReportNotificationService(auditNotificationUrl, auditRestemplate());
  }
}
