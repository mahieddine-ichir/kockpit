package com.accor.wcp.audit.it;

import com.accor.wcp.audit.AuditEvent;
import com.accor.wcp.audit.AuditModuleIntegration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FakeAuditModuleIntegration implements AuditModuleIntegration {

  @Override
  public String supportedType() {
    return "builtin.web";
  }

  @Override
  public void postProcessAuditEvents(List<AuditEvent> events) {
    log.info("postProcessAuditEvents (fake for test): {}", events);
  }
}
