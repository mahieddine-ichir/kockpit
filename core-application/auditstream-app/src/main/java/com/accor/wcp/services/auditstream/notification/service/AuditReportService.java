package com.accor.wcp.services.auditstream.notification.service;

import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.AuditReportRequestConsumer;
import com.accor.wcp.services.auditstream.notification.AuditReportHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Lists;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditReportService {

  private final ObjectMapper objectMapper;

  private final List<AuditReportRequestConsumer> auditReportRequestConsumers;

  @Value("${elasticsearch.indexation.partition.size:15}")
  private int indexationPartitionSize;

  @PostConstruct
  void init() {
    objectMapper.registerModule(new JavaTimeModule());
  }

  public void report(AuditReportRequest auditReportRequest) {
    report(List.of(auditReportRequest));
  }


  /**
   * @deprecated see {@link AuditReportHelper#computeTtl(AuditReportRequest)}
   */
  public static int computeTtl(AuditReportRequest auditReportRequest) {
    return AuditReportHelper.computeTtl(auditReportRequest);
  }

  public void report(List<AuditReportRequest> auditReportRequests) {
    // Group by x event (memory optimisation)
    Lists.partition(auditReportRequests, indexationPartitionSize).parallelStream().forEach(this::internalProcessAuditReports);
  }

  private void internalProcessAuditReports(List<AuditReportRequest> auditReportRequests) {
    auditReportRequestConsumers.forEach(auditReportRequestConsumer -> {
      try {
        auditReportRequestConsumer.accept(auditReportRequests);
      } catch (Exception e) {
          log.error("Error processing Audit reports, consumer class {}", auditReportRequestConsumer.getClass(), e);
      }
    });
  }
}
