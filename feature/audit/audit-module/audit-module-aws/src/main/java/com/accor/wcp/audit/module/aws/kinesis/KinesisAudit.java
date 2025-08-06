package com.accor.wcp.audit.module.aws.kinesis;

import com.accor.wcp.audit.AbstractAuditEvent;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@SuperBuilder
public class KinesisAudit extends AbstractAuditEvent {
  private String partitionKey;
  private String streamName;
  private String payload;

  public static void logKinesisQuery(KinesisAudit kinesisAudit) {
    log.debug(
        "\"partitionKey\":\"{}\"," + "\"streamName\":\"{}\"," + "\"payload\":{}",
        kinesisAudit.getPartitionKey(),
        kinesisAudit.getStreamName(),
        kinesisAudit.getPayload());
  }
}
