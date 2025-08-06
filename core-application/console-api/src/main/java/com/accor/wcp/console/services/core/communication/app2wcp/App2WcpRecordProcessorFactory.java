package com.accor.wcp.console.services.core.communication.app2wcp;

import com.accor.wcp.audit.AuditorEventService;
import com.accor.wcp.audit.AuditorKeyValueService;
import com.accor.wcp.audit.AuditorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;

@Component
@RequiredArgsConstructor
class App2WcpRecordProcessorFactory implements ShardRecordProcessorFactory {

  private final App2WcpNotificationService app2WcpNotificationService;

  private final AuditorService auditorService;
  private final AuditorKeyValueService auditorKeyValueService;
  private final AuditorEventService auditorEventService;

  @Override
  public ShardRecordProcessor shardRecordProcessor() {
    return new App2WcpRecordProcessor(app2WcpNotificationService,
            auditorService, auditorKeyValueService, auditorEventService);
  }
}
