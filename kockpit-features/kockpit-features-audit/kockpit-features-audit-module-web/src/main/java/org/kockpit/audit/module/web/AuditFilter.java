package org.kockpit.audit.module.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditorEventService;
import org.kockpit.audit.api.AuditorKeyValueService;
import org.kockpit.audit.api.AuditorService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.kockpit.audit.module.web.WebAuditKeyValues.getWebKeyValues;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class AuditFilter extends OncePerRequestFilter {

  private final List<SkipRequestAuditor> skipRequestAuditorList;

  private final List<RequestAuditor> requestAuditorList;

  private final List<ResponseAuditor> responseAuditorList;

  private final AuditorService auditorService;
  private final AuditorEventService auditorEvents;
  private final AuditorKeyValueService auditorKeyValues;

  public AuditFilter(
      List<SkipRequestAuditor> skipRequestAuditorList,
      List<RequestAuditor> requestAuditorList,
      List<ResponseAuditor> responseAuditorList,
      AuditorService auditorService,
      AuditorEventService auditorEvents,
      AuditorKeyValueService auditorKeyValues) {
    this.auditorService = auditorService;
    this.auditorEvents = auditorEvents;
    this.auditorKeyValues = auditorKeyValues;
    skipRequestAuditorList.sort(AnnotationAwareOrderComparator.INSTANCE);
    requestAuditorList.sort(AnnotationAwareOrderComparator.INSTANCE);
    responseAuditorList.sort(AnnotationAwareOrderComparator.INSTANCE);
    this.skipRequestAuditorList = skipRequestAuditorList;
    this.requestAuditorList = requestAuditorList;
    this.responseAuditorList = responseAuditorList;
  }

  @Override
  public void doFilterInternal(
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      FilterChain chain)
      throws IOException, ServletException {

    // Filter audit?
    if (isRequestNotAuditable(httpServletRequest, httpServletResponse)) {
      // Normal execution without audit mechanism
      chain.doFilter(httpServletRequest, httpServletResponse);
      return;
    }

    auditorService.startAudit();
    // Create a cache request / response wrappers to access request and response
    // Framework 7 : le constructeur sans limite a disparu ; Integer.MAX_VALUE = comportement 6.x (cache non borné)
    ContentCachingRequestWrapper requestWrapper =
        new ContentCachingRequestWrapper(httpServletRequest, Integer.MAX_VALUE);
    ContentCachingResponseWrapper responseWrapper =
        new ContentCachingResponseWrapper(httpServletResponse);
    try {
      chain.doFilter(requestWrapper, responseWrapper);
    } finally {
      WebAuditReportData auditReport =
              new WebAuditReportData(WebAuditEvent.builder().build(), new ArrayList<>());
      auditResponse(responseWrapper, auditReport);
      try {
        responseWrapper.copyBodyToResponse();
      } catch (Exception t) {
        log.error("Error copying body to http response.", t);
      }
      auditRequest(requestWrapper, auditReport);

      auditorEvents.addAuditEvents(WebAuditReportData.TYPE, List.of(auditReport.getWebAuditEvent()));
      auditorKeyValues.addIndexedKeyValues(getWebKeyValues(auditReport));
      auditorService.stopAuditAndNotify();
    }
  }

  private void auditRequest(
      ContentCachingRequestWrapper httpServletRequest, WebAuditReportData auditReport) {
    try {
      auditReport.getWebAuditEvent().setStartTime(System.currentTimeMillis());
      requestAuditorList.forEach(requestAuditor -> requestAuditor.audit(httpServletRequest, auditReport));
    } catch (Exception e) {
      log.error("PreAudit failed : {}", e.getMessage(), e);
    }
  }

  private void auditResponse(
      ContentCachingResponseWrapper httpServletResponse, WebAuditReportData auditReport) {
    try {
      auditReport.getWebAuditEvent().setEndTime(System.currentTimeMillis());
      responseAuditorList.forEach(responseAuditor -> responseAuditor.audit(httpServletResponse, auditReport));
    } catch (Exception e) {
      log.error("ExecutionAudit failed : {}", e.getMessage(), e);
    }
  }

  private boolean isRequestNotAuditable(HttpServletRequest request, HttpServletResponse response) {
    return skipRequestAuditorList.stream()
        .map(f -> f.skippingAudit(request, response))
        .filter(Boolean::booleanValue)
        .findFirst()
        .orElse(false);
  }
}
