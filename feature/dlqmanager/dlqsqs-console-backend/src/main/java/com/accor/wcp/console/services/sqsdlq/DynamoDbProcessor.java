package com.accor.wcp.console.services.sqsdlq;

import static com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2.*;
import static com.accor.wcp.console.services.sqsdlq.model.PartitionKey.KEY_SEPARATOR;
import static java.util.Objects.nonNull;
import static org.apache.camel.component.aws2.sqs.Sqs2Constants.MESSAGE_ATTRIBUTES;
import static org.apache.camel.component.aws2.sqs.Sqs2Constants.MESSAGE_ID;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.accor.wcp.console.services.sqsdlq.dynamo.SqsDocumentRepositoryV2;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.AttributeDocument;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsAttributeType;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentRetry;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentStatus;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2;
import com.accor.wcp.console.services.sqsdlq.model.PartitionKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.aws2.ddb.Ddb2Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

@Component
@RequiredArgsConstructor
@Slf4j
public class DynamoDbProcessor implements Processor {
  public static final String SQS_HEADER_SENT_TIMESTAMP = "SentTimestamp";
  public static final String SQS_HEADER_GROUP_ID = "MessageGroupId";
  public static final String WCP_RETRY_MESSAGE_PARENT_ID = "WCP_RETRY_MESSAGE_PARENT_ID";
  public static final String WCP_RETRY_MESSAGE_ID = "WCP_RETRY_MESSAGE_ID";

  private final SqsDocumentRepositoryV2 sqsDocumentRepositoryV2;

  @Override
  public void process(Exchange exchange) {
    PartitionKey partitionKey = extractPartitionKey(exchange);
    Object headerMessageAttributes = exchange.getIn().getHeader(MESSAGE_ATTRIBUTES);
    Map<String, MessageAttributeValue> messageAttributes =
        (Map<String, MessageAttributeValue>) headerMessageAttributes;
    if (messageAttributes != null && messageAttributes.containsKey(WCP_RETRY_MESSAGE_PARENT_ID)) {
      Optional<SqsDocumentV2> sqsDocumentOpt =
          sqsDocumentRepositoryV2.fetchOne(
              partitionKey, messageAttributes.get(WCP_RETRY_MESSAGE_PARENT_ID).stringValue());
      if (sqsDocumentOpt.isPresent()) {
        processRetry(exchange, messageAttributes, sqsDocumentOpt.get());
      } else {
        createNewSqsDocument(partitionKey, exchange);
      }
    } else {
      createNewSqsDocument(partitionKey, exchange);
    }
  }

  private void processRetry(
      Exchange exchange,
      Map<String, MessageAttributeValue> headerMessageAttributes,
      SqsDocumentV2 sqsDocument) {
    if (headerMessageAttributes.containsKey(WCP_RETRY_MESSAGE_ID)) {
      retryReturnInError(headerMessageAttributes, sqsDocument);
    } else {
      saveRetry(exchange, headerMessageAttributes, sqsDocument);
    }
  }

  private void saveRetry(
      Exchange exchange,
      Map<String, MessageAttributeValue> headerMessageAttributes,
      SqsDocumentV2 sqsDocument) {
    SqsDocumentRetry sqsDocumentRetry = new SqsDocumentRetry();

    sqsDocumentRetry.setBody((String) exchange.getIn().getBody());
    sqsDocumentRetry.setSentTimestamp(
        (String) exchange.getIn().getHeader(SQS_HEADER_SENT_TIMESTAMP));

    Object headerGroupId = exchange.getIn().getHeader(SQS_HEADER_GROUP_ID);
    if (headerGroupId != null && isNotBlank((String) headerGroupId)) {
      sqsDocumentRetry.setGroupId((String) headerGroupId);
    }

    List<AttributeDocument> attributes = new ArrayList<>();
    headerMessageAttributes
        .entrySet()
        .forEach(
            e -> {
              if (!e.getKey().equals(WCP_RETRY_MESSAGE_PARENT_ID)) {
                SqsAttributeType attributeType;
                String value = null;
                if ("Number".equals(e.getValue().dataType())) {
                  attributeType = SqsAttributeType.NUMBER;
                  value = e.getValue().stringValue();
                } else if ("Binary".equals(e.getValue().dataType())) {
                  attributeType = SqsAttributeType.BINARY;
                  value = e.getValue().binaryValue().asUtf8String();
                } else {
                  attributeType = SqsAttributeType.STRING;
                  value = e.getValue().stringValue();
                }
                attributes.add(
                    new AttributeDocument(e.getKey(), attributeType, value));
              }
            });

    sqsDocumentRetry.setAttributes(attributes);
    sqsDocument.getRetries().add(sqsDocumentRetry);
    sqsDocumentRepositoryV2.update(sqsDocument);
  }

  private void retryReturnInError(
      Map<String, MessageAttributeValue> headerMessageAttributes, SqsDocumentV2 sqsDocument) {
    sqsDocument.getRetries().stream()
        .forEach(
            retry ->
                retry.getAttributes().stream()
                    .forEach(
                        attribute -> {
                          if (attribute.getName().equals(WCP_RETRY_MESSAGE_ID)
                              && attribute
                                  .getValue()
                                  .equals(
                                      headerMessageAttributes
                                          .get(WCP_RETRY_MESSAGE_ID)
                                          .stringValue())) {
                            retry.setStatus("Error");
                            retry.setReceiveTime(new Date().getTime());
                          }
                        }));

    sqsDocumentRepositoryV2.update(sqsDocument);
  }

  private void createNewSqsDocument(PartitionKey partitionKey, Exchange exchange) {
    Map<String, AttributeValue> newDdbItem = new HashMap<>();
    String sentTimestamp = (String) exchange.getIn().getHeader(SQS_HEADER_SENT_TIMESTAMP);
    String id = sentTimestamp + KEY_SEPARATOR + exchange.getIn().getHeader(MESSAGE_ID);

    newDdbItem.put(
        ATTRIBUTE_NAME_PARTITION_KEY, AttributeValue.builder().s(partitionKey.toString()).build());
    newDdbItem.put(ATTRIBUTE_NAME_ID, AttributeValue.builder().s(id).build());

    Object body = exchange.getIn().getBody();
    if (nonNull(body) && StringUtils.isNotBlank(String.valueOf(body))) {
      newDdbItem.put(ATTRIBUTE_NAME_BODY, AttributeValue.builder().s(String.valueOf(body)).build());
    }
    if (isNoneBlank(sentTimestamp)) {
      newDdbItem.put(ATTRIBUTE_NAME_SENT_TIME, AttributeValue.builder().n(sentTimestamp).build());
    }

    Object headerGroupId = exchange.getIn().getHeader(SQS_HEADER_GROUP_ID);
    if (nonNull(headerGroupId) && isNotBlank((String) headerGroupId)) {
      newDdbItem.put(
          ATTRIBUTE_NAME_GROUP_ID,
          AttributeValue.builder().s(String.valueOf(headerGroupId)).build());
    }

    Object headerMessageAttributes = exchange.getIn().getHeader(MESSAGE_ATTRIBUTES);

    if (nonNull(headerMessageAttributes)
        && !CollectionUtils.isEmpty((Map<String, MessageAttributeValue>) headerMessageAttributes)) {

      List<AttributeValue> messageAttributesList = new ArrayList<>();
      ((Map<String, MessageAttributeValue>) headerMessageAttributes)
          .entrySet()
          .forEach(
              e -> {
                Map<String, AttributeValue> attribute = new HashMap<>();
                String dataType = e.getValue().dataType();
                if ("Binary".equalsIgnoreCase(dataType)) {
                  attribute.put(
                      AttributeDocument.ATTRIBUTE_NAME,
                      AttributeValue.builder().s(e.getKey()).build());
                  attribute.put(
                      AttributeDocument.ATTRIBUTE_TYPE,
                      AttributeValue.builder().s(dataType).build());
                  attribute.put(
                      AttributeDocument.ATTRIBUTE_VALUE,
                      AttributeValue.builder().s(e.getValue().binaryValue().asUtf8String()).build());
                } else {
                  attribute.put(
                      AttributeDocument.ATTRIBUTE_NAME,
                      AttributeValue.builder().s(e.getKey()).build());
                  attribute.put(
                      AttributeDocument.ATTRIBUTE_TYPE,
                      AttributeValue.builder().s(dataType).build());
                  attribute.put(
                      AttributeDocument.ATTRIBUTE_VALUE,
                      AttributeValue.builder().s(e.getValue().stringValue()).build());
                }
                messageAttributesList.add(AttributeValue.builder().m(attribute).build());
              });

      newDdbItem.put(
          ATTRIBUTE_NAME_ATTRIBUTES, AttributeValue.builder().l(messageAttributesList).build());
    }

    newDdbItem.put(
        ATTRIBUTE_STATUS_ATTRIBUTES,
        AttributeValue.builder().s(SqsDocumentStatus.NEW.getValue()).build());

    exchange.getIn().setHeader(Ddb2Constants.ITEM, newDdbItem);
  }

  private PartitionKey extractPartitionKey(Exchange exchange) {
    String domain = (String) exchange.getIn().getHeader(SqsRouteBuilder.HEADER_DOMAIN);
    String env = (String) exchange.getIn().getHeader(SqsRouteBuilder.HEADER_ENVIRONMENT);
    String queueName = (String) exchange.getIn().getHeader(SqsRouteBuilder.HEADER_QUEUE_NAME);
    return PartitionKey.builder().domain(domain).env(env).queueName(queueName).build();
  }
}
