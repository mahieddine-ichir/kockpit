package com.accor.wcp.services.auditstream.notification.insights;

import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.AuditReportRequestConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("insights")
@Slf4j
public class AuditReportRequestConsumerForInsights implements AuditReportRequestConsumer {

    private final InsightIndexEsRepository insightIndexEsRepository;

    @Override
    public void accept(List<AuditReportRequest> auditReportRequests) {
        try {
            insightIndexEsRepository.bulkIndexRequests(auditReportRequests);
        } catch (Exception e) {
            log.error("Error processing data for Insights", e);
        }
    }
}
