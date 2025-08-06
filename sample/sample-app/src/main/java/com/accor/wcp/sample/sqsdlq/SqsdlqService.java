package com.accor.wcp.sample.sqsdlq;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

@Component
public class SqsdlqService {

    private final SqsClient sqsClient;

    @Value("${sqsdlq.queueUrl:}")
    private String samplesQueueUrl;

    @Value("${sqsdlq-fifo.queueUrl:}")
    private String samplesFifoQueueUrl;

    public SqsdlqService(@Qualifier("sqsdlqClient") SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public void sendToSqsdlq() {
        Sqsdlq sqsdlqMessage = constructSqsdlqMessage();
        SendMessageRequest messageRequest = getSendMessageRequest(samplesQueueUrl, sqsdlqMessage);
        sendMessage(samplesQueueUrl, messageRequest);
    }

    public void sendToSqsdlqFifo() {
        Sqsdlq sqsdlqMessage = constructSqsdlqMessage();
        SendMessageRequest messageRequest = getSendMessageRequestToFifoSqs(samplesFifoQueueUrl, sqsdlqMessage);
        sendMessage(samplesFifoQueueUrl, messageRequest);
    }

    private Sqsdlq constructSqsdlqMessage() {
        MessageAttributeValue msgAttributeValue1 = MessageAttributeValue.builder()
            .stringValue("Sqsdlq")
            .dataType("String")
            .build();

        MessageAttributeValue msgAttributeValue2 = MessageAttributeValue.builder()
            .stringValue("Lambda")
            .dataType("String")
            .build();

        Map<String, MessageAttributeValue> attributes = Map.of("FirstAttribute", msgAttributeValue1, "SecondAttribute", msgAttributeValue2);

        return new Sqsdlq("{\"error\":true,\n\"service\":\"Sqsdlq\",\n\"test\":\"wcp-samples\"}", attributes);
    }

    public void sendMessage(String queueUrl, SendMessageRequest sendMessageRequest) {
        SendMessageResponse sendResponse = sqsClient.sendMessage(sendMessageRequest);

        if (!isMessageDelivered(sendResponse)) {
            String message =
                    String.format(
                            "Message was not delivered properly to queue %s : %s", queueUrl, sendResponse);
            throw SqsException.builder().message(message).build();
        }
    }

    private SendMessageRequest getSendMessageRequest(String queueUrl, Sqsdlq sqsdlqMessage) {
        return SendMessageRequest.builder()
            .messageBody(sqsdlqMessage.getBody())
            .queueUrl(queueUrl)
            .messageAttributes(sqsdlqMessage.getAttributes())
            .build();
    }

    private SendMessageRequest getSendMessageRequestToFifoSqs(String queueUrl, Sqsdlq sqsdlqMessage) {
        return SendMessageRequest.builder()
                .messageBody(sqsdlqMessage.getBody())
                .queueUrl(queueUrl)
                .messageAttributes(sqsdlqMessage.getAttributes())
                .messageGroupId("message-group-id")
                .messageDeduplicationId(sha256Hex(sqsdlqMessage.getBody()))
                .build();
    }

    private boolean isMessageDelivered(SendMessageResponse sendResponse) {
        return Objects.nonNull(sendResponse) && Objects.nonNull(sendResponse.messageId());
    }

    public void sendToSqsdlqWithBinary() {
        Sqsdlq sqsdlqMessage = constructSqsdlqMessage();

        MessageAttributeValue msgAttributeValue1 = MessageAttributeValue.builder()
            .stringValue("Sqsdlq")
            .dataType("String")
            .build();

        MessageAttributeValue msgAttributeValue2 = MessageAttributeValue.builder()
            .stringValue("Lambda")
            .dataType("String")
            .build();

        MessageAttributeValue msgAttributeValue3 = MessageAttributeValue.builder()
            .binaryValue(SdkBytes.fromByteArray("sample string data to bytes".getBytes()))
            .dataType("Binary")
            .build();

        Map<String, MessageAttributeValue> attributes = Map.of("FirstAttribute", msgAttributeValue1,
            "SecondAttribute", msgAttributeValue2,
            "BinaryAttribute", msgAttributeValue3);

        sqsdlqMessage.setAttributes(attributes);
        SendMessageRequest messageRequest = getSendMessageRequest(samplesQueueUrl, sqsdlqMessage);
        sendMessage(samplesQueueUrl, messageRequest);
    }
}
