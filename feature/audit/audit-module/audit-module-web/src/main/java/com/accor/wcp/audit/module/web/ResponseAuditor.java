package com.accor.wcp.audit.module.web;

import org.springframework.web.util.ContentCachingResponseWrapper;

/** Audit component definition. It executes after treating request internally. */
public interface ResponseAuditor {

  void audit(ContentCachingResponseWrapper response, WebAuditReportData webAuditReport);
}
