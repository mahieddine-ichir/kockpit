package com.accor.wcp.services.auditstream.notification.kinesis;

import com.accor.wcp.services.auditstream.notification.service.AuditReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;

@Component
@RequiredArgsConstructor
class RecordProcessorFactory implements ShardRecordProcessorFactory {
  private final AuditRequestConverter auditRequestConverter;
  private final AuditReportService auditReportService;

  @Override
  public ShardRecordProcessor shardRecordProcessor() {
    return new RecordProcessor(auditRequestConverter, auditReportService);
  }
}
