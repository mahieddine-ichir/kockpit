package com.accor.wcp.sample.audit.sqs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

@Component
public class AuditSqsService {

  private final SqsClient sqsClient;

  @Value("${sqs.queueUrl:}")
  private String queueUrl;

  public AuditSqsService(@Qualifier("sqsClient") SqsClient sqsClient) {
    this.sqsClient = sqsClient;
  }

  public List<String> sendToSqs(int times) {
    List<String> results = new ArrayList<>();

    MessageAttributeValue msgAttributeValue1 =
        MessageAttributeValue.builder().stringValue("MessageSend").dataType("String").build();

    MessageAttributeValue msgAttributeValue2 =
        MessageAttributeValue.builder().stringValue("Sqs").dataType("String").build();

    for (int i = 0; i < times; i++) {
      Map<String, MessageAttributeValue> attributes =
          Map.of("FirstAttribute", msgAttributeValue1, "SecondAttribute", msgAttributeValue2);
      SqsMessage sqsMessage = new SqsMessage("{'sqsMessage':'sqsMessage" + i + "'}", attributes);
      results.add("Send sqs message " + i + " times");
      sendMessage(queueUrl, sqsMessage);
    }
    return results;
  }

  public void sendMessage(String queueUrl, SqsMessage sqsMessage) {
    SendMessageRequest messageRequest = getSendMessageRequest(queueUrl, sqsMessage);
    SendMessageResponse sendResponse = sqsClient.sendMessage(messageRequest);

    if (!isMessageDelivered(sendResponse)) {
      String message =
          String.format(
              "Message was not delivered properly to queue %s : %s", queueUrl, sendResponse);
      throw SqsException.builder().message(message).build();
    }
  }

  private SendMessageRequest getSendMessageRequest(String queueUrl, SqsMessage sqsMessage) {
    return SendMessageRequest.builder()
        .messageBody(sqsMessage.getPayload())
        .queueUrl(queueUrl)
        .messageAttributes(sqsMessage.getAttributes())
        .build();
  }

  private boolean isMessageDelivered(SendMessageResponse sendResponse) {
    return Objects.nonNull(sendResponse) && Objects.nonNull(sendResponse.messageId());
  }
}
