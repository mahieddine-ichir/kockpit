package com.accor.wcp.audit.notification.autoconfigure.service;

import static java.util.Objects.nonNull;

import com.accor.wcp.audit.AuditModuleActivator;
import com.accor.wcp.sdk.application.service.ServiceApplicationIntegration;
import com.accor.wcp.sdk.application.service.ServiceApplicationState;
import com.accor.wcp.sdk.application.service.ServiceMessageNotification;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AuditApplicationServiceIntegration implements ServiceApplicationIntegration {

  private final List<AuditModuleActivator> auditModuleActivators;

  @Override
  public String getServiceId() {
    return "audit";
  }

  @Override
  public void notification(ServiceMessageNotification notification) {
    log.info("Notification for audit: {}", notification);
  }

  @Override
  public ServiceApplicationState initialize() {
    if (nonNull(auditModuleActivators)) {
      auditModuleActivators.forEach(AuditModuleActivator::initialize);
    }
    return ServiceApplicationState.INITIALIZING;
  }

  @Override
  public ServiceApplicationState getState() {
    return ServiceApplicationState.RUNNING;
  }

  @Override
  public ServiceApplicationState stop() {
    if (nonNull(auditModuleActivators)) {
      auditModuleActivators.forEach(AuditModuleActivator::stop);
    }
    return ServiceApplicationState.STOPPED;
  }
}
