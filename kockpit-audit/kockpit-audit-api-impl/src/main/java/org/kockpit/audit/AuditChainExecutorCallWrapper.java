package org.kockpit.audit;

import org.kockpit.audit.api.AuditReport;

import java.util.Map;

public class AuditChainExecutorCallWrapper implements ChainExecutorCallWrapper {

    @Override
    public void initContext(Map<Object, Object> context) {
        AuditReport parentAuditReport = AuditReportContainer.AUDIT_REPORT_HOLDER.get();
        context.put(AuditReport.class, parentAuditReport);
    }

    @Override
    public void beforeExecution(Map<Object, Object> context) {
        AuditReportContainer.AUDIT_REPORT_HOLDER.set((AuditReport) context.get(AuditReport.class));
    }

    @Override
    public void releaseAfterExecution(Map<Object, Object> context) {
        AuditReportContainer.AUDIT_REPORT_HOLDER.remove();
    }
}
