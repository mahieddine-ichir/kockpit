package com.accor.wcp.console.services.sqsdlq;

import static com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2.ATTRIBUTE_NAME_ATTRIBUTES;
import static com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2.ATTRIBUTE_NAME_BODY;
import static com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2.ATTRIBUTE_NAME_GROUP_ID;
import static com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2.ATTRIBUTE_NAME_ID;
import static com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2.ATTRIBUTE_NAME_PARTITION_KEY;
import static com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2.ATTRIBUTE_NAME_SENT_TIME;
import static org.assertj.core.api.Assertions.assertThat;

import com.accor.wcp.console.services.sqsdlq.dynamo.SqsDocumentRepositoryV2;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsAttributeType;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.component.aws2.ddb.Ddb2Constants;
import org.apache.camel.component.aws2.sqs.Sqs2Constants;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

class DynamoDbProcessorTest {

  @MockBean private SqsDocumentRepositoryV2 sqsDocumentRepository;

  private final DynamoDbProcessor dynamoDbProcessor = new DynamoDbProcessor(sqsDocumentRepository);

  @Test
  public void should_process_ddb_item_from_sqs_body_and_headers() {
    // GIVEN
    Exchange exchange = getExchange();

    // WHEN
    dynamoDbProcessor.process(exchange);

    // THEN
    assertThat(exchange.getIn().getHeader(Ddb2Constants.ITEM)).isNotNull();
    Map<String, AttributeValue> newDdbItem =
        (Map<String, AttributeValue>) exchange.getIn().getHeader(Ddb2Constants.ITEM);
    assertThat(newDdbItem.get(ATTRIBUTE_NAME_PARTITION_KEY).s())
        .isEqualTo("mockDomain_mockEnv_mockQueueName");
    assertThat(newDdbItem.get(ATTRIBUTE_NAME_ID).s()).isEqualTo("12454897_MESSAGE_ID");
    assertThat(newDdbItem.get(ATTRIBUTE_NAME_BODY).s()).isEqualTo("BODY");
    assertThat(newDdbItem.get(ATTRIBUTE_NAME_SENT_TIME).n()).isEqualTo("12454897");
    assertThat(newDdbItem.get(ATTRIBUTE_NAME_GROUP_ID).s()).isEqualTo("SQS_HEADER_GROUP_ID");
    assertThat(newDdbItem.get(ATTRIBUTE_NAME_ATTRIBUTES).l().get(0).m())
        .contains(Map.entry("name", AttributeValue.builder().s("ConsoleServiceKey").build()));
    assertThat(newDdbItem.get(ATTRIBUTE_NAME_ATTRIBUTES).l().get(0).m())
        .contains(Map.entry("value", AttributeValue.builder().s("Value").build()));
    assertThat(newDdbItem.get(ATTRIBUTE_NAME_ATTRIBUTES).l().get(0).m())
        .contains(
            Map.entry(
                "type", AttributeValue.builder().s(SqsAttributeType.STRING.getValue()).build()));
  }

  @Test
  public void should_process_ddb_item_from_sqs_body_and_headers_when_no_group_id_and_attributes() {
    // GIVEN
    Exchange exchange = getExchange();
    exchange.getIn().setHeader(Sqs2Constants.MESSAGE_ATTRIBUTES, null);
    exchange.getIn().setHeader(DynamoDbProcessor.SQS_HEADER_GROUP_ID, null);

    // WHEN
    dynamoDbProcessor.process(exchange);

    // THEN
    assertThat(exchange.getIn().getHeader(Ddb2Constants.ITEM)).isNotNull();
    Map<String, AttributeValue> newDdbItem =
        (Map<String, AttributeValue>) exchange.getIn().getHeader(Ddb2Constants.ITEM);
    assertThat(newDdbItem.get(ATTRIBUTE_NAME_PARTITION_KEY).s())
        .isEqualTo("mockDomain_mockEnv_mockQueueName");
    assertThat(newDdbItem.get(ATTRIBUTE_NAME_ID).s()).isEqualTo("12454897_MESSAGE_ID");
    assertThat(newDdbItem.get(ATTRIBUTE_NAME_BODY).s()).isEqualTo("BODY");
    assertThat(newDdbItem.get(ATTRIBUTE_NAME_SENT_TIME).n()).isEqualTo("12454897");
    assertThat(newDdbItem.get(ATTRIBUTE_NAME_GROUP_ID)).isNull();
    assertThat(newDdbItem.get(ATTRIBUTE_NAME_ATTRIBUTES)).isNull();
  }

  private Exchange getExchange() {
    Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    exchange.getIn().setHeader(SqsRouteBuilder.HEADER_DOMAIN, "mockDomain");
    exchange.getIn().setHeader(SqsRouteBuilder.HEADER_ENVIRONMENT, "mockEnv");
    exchange.getIn().setHeader(SqsRouteBuilder.HEADER_QUEUE_NAME, "mockQueueName");
    exchange.getIn().setBody("BODY");
    exchange.getIn().setHeader(Sqs2Constants.MESSAGE_ID, "MESSAGE_ID");
    exchange.getIn().setHeader(DynamoDbProcessor.SQS_HEADER_GROUP_ID, "SQS_HEADER_GROUP_ID");
    exchange
        .getIn()
        .setHeader(
            Sqs2Constants.MESSAGE_ATTRIBUTES,
            Map.of(
                "ConsoleServiceKey",
                MessageAttributeValue.builder()
                    .stringValue("Value")
                    .dataType(SqsAttributeType.STRING.getValue())
                    .build()));

    exchange.getIn().setHeader(DynamoDbProcessor.SQS_HEADER_SENT_TIMESTAMP, "12454897");

    return exchange;
  }
}
