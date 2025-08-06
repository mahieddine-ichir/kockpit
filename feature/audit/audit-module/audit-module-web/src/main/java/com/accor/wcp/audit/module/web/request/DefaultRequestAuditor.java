package com.accor.wcp.audit.module.web.request;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.firstNonBlank;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.accor.wcp.audit.module.web.RequestAuditor;
import com.accor.wcp.audit.module.web.SecurityUtils;
import com.accor.wcp.audit.module.web.WebAuditEvent;
import com.accor.wcp.audit.module.web.WebAuditReportData;
import jakarta.servlet.http.HttpServletRequest;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.ContentCachingRequestWrapper;

/** Resolve http method and set it to audit data. */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class DefaultRequestAuditor implements RequestAuditor {

  private final String originHeaderName;

  private final String traceIdHeaderName;
  private final boolean filterAuthorizationHeader;

  public DefaultRequestAuditor(
      String originHeaderName, String traceIdHeaderName, boolean filterAuthorizationHeader) {
    this.originHeaderName = originHeaderName;
    this.traceIdHeaderName = traceIdHeaderName;
    this.filterAuthorizationHeader = filterAuthorizationHeader;
  }

  @SneakyThrows
  @Override
  public void audit(ContentCachingRequestWrapper request, WebAuditReportData auditReport) {
    Object traceIdRequestAttribute = request.getAttribute(traceIdHeaderName);
    String traceIdRequestAttributeString =
        nonNull(traceIdRequestAttribute) ? traceIdRequestAttribute.toString() : null;
    WebAuditEvent webAuditEvent = auditReport.getWebAuditEvent();
    webAuditEvent.setTraceId(
        firstNonBlank(request.getHeader(traceIdHeaderName), traceIdRequestAttributeString));
    webAuditEvent.setOrigin(firstNonBlank(request.getHeader(originHeaderName), ""));
    webAuditEvent.setHttpAuditedRequest(
        HttpAuditedRequest.builder()
            .principal(SecurityUtils.getCurrentUserLogin().orElse("anonymousUser"))
            .method(request.getMethod())
            .headers(getHeaders(request))
            .uri(request.getRequestURI())
            .body(new String(request.getContentAsByteArray()))
            .params(getParameters(request.getParameterMap()))
            .build());
  }

  Map<String, List<String>> getParameters(Map<String, String[]> parameterMap) {
    return parameterMap.entrySet().stream()
        .map(
            stringEntry -> new SimpleEntry<>(stringEntry.getKey(), List.of(stringEntry.getValue())))
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  HttpHeaders getHeaders(HttpServletRequest httpServletRequest) {
    HttpHeaders httpHeaders = new HttpHeaders();
    Collections.list(httpServletRequest.getHeaderNames()).stream()
        .filter(hd -> !filterAuthorizationHeader || !AUTHORIZATION.equalsIgnoreCase(hd))
        .forEach(hd -> {
          String headerValue;
          try {
            headerValue = httpServletRequest.getHeader(hd);
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
