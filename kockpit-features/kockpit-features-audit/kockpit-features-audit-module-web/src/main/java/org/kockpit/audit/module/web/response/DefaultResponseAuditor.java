package org.kockpit.audit.module.web.response;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.module.web.ResponseAuditor;
import org.kockpit.audit.module.web.WebAuditEvent;
import org.kockpit.audit.module.web.WebAuditReportData;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.ContentCachingResponseWrapper;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/** Resolve http method and set it to audit data. */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@RequiredArgsConstructor
public class DefaultResponseAuditor implements ResponseAuditor {

  private final boolean filterAuthorizationHeader;

  @Override
  public void audit(ContentCachingResponseWrapper response, WebAuditReportData auditReport) {
    WebAuditEvent webAuditEvent = auditReport.getWebAuditEvent();
    webAuditEvent.setHttpAuditedResponse(
        HttpAuditedResponse.builder()
            .headers(getHeaders(response))
            .body(new String(response.getContentAsByteArray()))
            .status(response.getStatus())
            .build());
  }

  private HttpHeaders getHeaders(HttpServletResponse response) {
    HttpHeaders httpHeaders = new HttpHeaders();
    response.getHeaderNames().stream()
        .filter(hd -> !filterAuthorizationHeader || !AUTHORIZATION.equalsIgnoreCase(hd))
        .forEach(hd -> {
          String headerValue;
          try {
            headerValue = response.getHeader(hd);
          } catch (Exception e) {
            headerValue = "Error getting value";
            log.warn("Error getting header value for audit. headerName: {}, e: {}", hd, e.getMessage());
          }
          try {
            httpHeaders.add(hd, headerValue);
          } catch (Exception e) {
            log.warn("Error adding header to audit for headerName: {}, value: {}", hd, headerValue);
          }
        });
    return httpHeaders;
  }
}
