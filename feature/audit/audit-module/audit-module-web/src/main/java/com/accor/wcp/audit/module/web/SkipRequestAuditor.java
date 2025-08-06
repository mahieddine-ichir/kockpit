package com.accor.wcp.audit.module.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Audit component definition. It filters request that must be audited or not. */
public interface SkipRequestAuditor {
  boolean skippingAudit(HttpServletRequest request, HttpServletResponse response);
}
