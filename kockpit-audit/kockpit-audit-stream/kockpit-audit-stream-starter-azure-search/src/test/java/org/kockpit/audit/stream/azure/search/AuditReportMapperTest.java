package org.kockpit.audit.stream.azure.search;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kockpit.audit.stream.api.AuditReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(AuditReportMapperImpl.class)
class AuditReportMapperTest {

    @Autowired
    AuditReportMapper mapper;

    @Test
    void on_audit_report() {
        AuditReport auditReport = new AuditReport();
        auditReport.setAppId("appId");

        SearchAuditReport map = mapper.map(auditReport);
        Assertions.assertEquals("appId", map.getAppId());

    }

}