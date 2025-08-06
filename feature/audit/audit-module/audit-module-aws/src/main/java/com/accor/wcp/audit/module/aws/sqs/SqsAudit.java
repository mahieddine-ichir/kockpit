package com.accor.wcp.audit.module.aws.sqs;

import com.accor.wcp.audit.AbstractAuditEvent;
import java.util.Map;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Data
@SuperBuilder
@Slf4j
public class SqsAudit extends AbstractAuditEvent {

  private String queueUrl;
  private String issue;
  private String payload;
  private String messageId;
  private String groupId;
  private String deduplicationId;
  private Map<String, String> attributes;

  public static void logSqsQuery(SqsAudit event) {
    log.debug(
        "\"queueUrl\":\"{}\","
            + "\"issue\":\"{}\","
            + "\"payload\":{},"
            + "\"messageId\":{}"
            + "\"groupId\":{}"
            + "\"deduplicationId\":{}"
            + "\"attributes\":{}",
        event.getQueueUrl(),
        event.getIssue(),
        event.getPayload(),
        event.getMessageId(),
        event.getGroupId(),
        event.getDeduplicationId(),
        event.getAttributes());
  }
}
