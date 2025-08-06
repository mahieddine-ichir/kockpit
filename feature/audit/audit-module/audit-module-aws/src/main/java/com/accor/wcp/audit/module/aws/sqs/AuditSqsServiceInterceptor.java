package com.accor.wcp.audit.module.aws.sqs;

import static com.accor.wcp.audit.module.aws.sqs.SqsAudit.logSqsQuery;
import static java.util.Objects.nonNull;

import com.accor.wcp.audit.AuditNotStartedException;
import com.accor.wcp.audit.AuditorEventService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import software.amazon.awssdk.core.interceptor.Context.AfterExecution;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Slf4j
public class AuditSqsServiceInterceptor implements ExecutionInterceptor {

  static ApplicationContext context;

  public static void setApplicationContext(ApplicationContext context) {
    AuditSqsServiceInterceptor.context = context;
  }

  @Override
  public void afterExecution(AfterExecution context, ExecutionAttributes executionAttributes) {
    if (nonNull(context)
        && context.request() instanceof SendMessageRequest
        && context.response() instanceof SendMessageResponse) {
      this.auditSQSInformation(context);
    }
  }

  private void auditSQSInformation(AfterExecution execution) {
    SendMessageRequest sendMessageRequest = (SendMessageRequest) execution.request();
    SendMessageResponse sendMessageResponse = (SendMessageResponse) execution.response();

    AuditorEventService auditorEvents = context.getBean(AuditorEventService.class);

    try {
      SqsAudit sqsAuditEvent =
          SqsAudit.builder()
              .startTime(System.currentTimeMillis())
              .endTime(System.currentTimeMillis())
              .queueUrl(sendMessageRequest.queueUrl())
              .issue(getQueryIssue(sendMessageResponse))
              .payload(sendMessageRequest.messageBody())
              .messageId(getMessageId(sendMessageResponse))
              .groupId(getGroupId(sendMessageRequest))
              .deduplicationId(getMessageDeduplicationId(sendMessageRequest))
              .attributes(getMessageAttributes(sendMessageRequest.messageAttributes()))
              .build();
      logSqsQuery(sqsAuditEvent);
      auditorEvents.addAuditEvents("builtin.sqs-service", List.of(sqsAuditEvent));
    } catch (AuditNotStartedException ex) {
      log.info("Audit not started, ignoring this");
    }
  }

  private String getQueryIssue(SendMessageResponse sendMessageResponse) {
    return nonNull(getMessageId(sendMessageResponse)) ? "OK" : "KO";
  }

  private String getMessageId(SendMessageResponse sendResponse) {
    if (nonNull(sendResponse) && nonNull(sendResponse.messageId())) {
      return sendResponse.messageId();
    }
    return "";
  }

  private Map<String, String> getMessageAttributes(
      Map<String, MessageAttributeValue> messageAttributes) {
    if (!messageAttributes.isEmpty()) {
      return messageAttributes.entrySet().stream()
          .collect(Collectors.toMap(Map.Entry::getKey, this::messageAttributeValue));
    }
    return Collections.emptyMap();
  }

  private String messageAttributeValue(Entry<String, MessageAttributeValue> e) {
    String dataType = e.getValue().dataType();

    // Special case for binary attribute value
    if ("Binary".equalsIgnoreCase(dataType)) {
      return e.getValue().binaryValue().asUtf8String();
    }

    // Default is String value
    return e.getValue().stringValue();
  }

  private String getMessageDeduplicationId(SendMessageRequest request) {
    if (nonNull(request) && nonNull(request.messageDeduplicationId())) {
      return request.messageDeduplicationId();
    }
    return "";
  }

  private String getGroupId(SendMessageRequest request) {
    if (nonNull(request) && nonNull(request.messageGroupId())) {
      return request.messageGroupId();
    }
    return "";
  }
}
