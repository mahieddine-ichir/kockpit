package org.kockpit.audit.module.web;

import org.springframework.web.util.ContentCachingRequestWrapper;

/** Audit component definition. It executes before treating request. */
public interface RequestAuditor {

  void audit(ContentCachingRequestWrapper request, WebAuditReportData webAuditReport);
}
