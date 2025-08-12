package com.accor.wcp.services.auditstream.notification.insights;

import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.insights.mappings.InsightDataMapper;
import com.accor.wcp.services.auditstream.notification.insights.model.InsightIndexRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InsightIndexEsRepository {

  private final InsightIndexEsManager4Opensearch bulkInsightIndexRequests;

  private final InsightDataMapper insightDataMapper;

  void bulkIndexRequests(List<AuditReportRequest> indexRequests) {
    List<InsightIndexRequest> insights = insightDataMapper.toInsight(indexRequests);
    bulkInsightIndexRequests.bulkInsightIndexRequests(insights);
  }
}
