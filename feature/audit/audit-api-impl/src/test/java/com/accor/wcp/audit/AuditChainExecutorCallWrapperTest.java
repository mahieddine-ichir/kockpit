package com.accor.wcp.audit;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuditChainExecutorCallWrapperTest {

    AuditChainExecutorCallWrapper underTest = new AuditChainExecutorCallWrapper();

    @Test
    void should_inherit_audit_report() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        WrapDelegateExecutorService wrapDelegateExecutorService = new WrapDelegateExecutorService(executorService, underTest);
        AuditReport auditReport = AuditReport.builder()
                .build();
        AuditReportContainer.setAuditReport(auditReport);

        wrapDelegateExecutorService.execute(() -> {
            assertNotNull(AuditReportContainer.getAuditReport());
        });

    }

}