package org.kockpit.audit;

import org.kockpit.audit.api.Audit;
import org.kockpit.audit.api.AuditModuleIntegration;
import org.kockpit.audit.api.AuditReport;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

/**
 * Fixme check usage?
 */
public class AuditPostProcessor {

  private final Map<String, List<AuditModuleIntegration>> moduleIntegrationsByType;

  private final AuditObfuscationService auditObfuscationService;

  public AuditPostProcessor(
      List<AuditModuleIntegration> auditModuleIntegrations,
      AuditObfuscationService auditObfuscationService) {
    moduleIntegrationsByType =
        auditModuleIntegrations.stream().collect(groupingBy(AuditModuleIntegration::supportedType));
    this.auditObfuscationService = auditObfuscationService;
  }

  AuditReport process(AuditReport auditReport) {
    // Post process
    postProcessAuditModules(auditReport);

    // Obfuscate
    auditObfuscationService.obfuscate(auditReport);

    return auditReport;
  }

  private void postProcessAuditModules(AuditReport auditReport) {
    auditReport.getAudits().forEach(this::processAuditModule);
  }

  private void processAuditModule(Audit audit) {
    if (!moduleIntegrationsByType.containsKey(audit.getType())) {
      return;
    }
    List<AuditModuleIntegration> auditModuleIntegrations =
        moduleIntegrationsByType.get(audit.getType());
    auditModuleIntegrations.forEach(
        auditModuleIntegration -> auditModuleIntegration.postProcessAuditEvents(audit.getEvents()));
  }
}
