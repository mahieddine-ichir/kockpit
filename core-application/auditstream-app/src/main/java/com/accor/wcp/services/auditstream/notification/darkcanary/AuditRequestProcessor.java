package com.accor.wcp.services.auditstream.notification.darkcanary;

import com.accor.wcp.services.auditstream.notification.darkcanary.model.ConfiguredAuditReportRequest;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.ConfiguredDarkCanaryIndexDocument;

import java.util.List;

public interface AuditRequestProcessor {

    List<ConfiguredDarkCanaryIndexDocument> process(List<ConfiguredAuditReportRequest> auditReportRequests);
}
