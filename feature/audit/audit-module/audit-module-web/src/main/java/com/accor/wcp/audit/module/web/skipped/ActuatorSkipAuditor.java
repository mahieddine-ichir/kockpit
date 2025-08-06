package com.accor.wcp.audit.module.web.skipped;

import com.accor.wcp.audit.module.web.SkipRequestAuditor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Skip audit on actuator request */
public class ActuatorSkipAuditor implements SkipRequestAuditor {
  @Override
  public boolean skippingAudit(HttpServletRequest request, HttpServletResponse response) {
    return (request.getRequestURI().contains("actuator"));
  }
}
