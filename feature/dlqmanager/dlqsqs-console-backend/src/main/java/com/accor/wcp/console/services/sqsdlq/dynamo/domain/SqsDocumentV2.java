package com.accor.wcp.console.services.sqsdlq.dynamo.domain;

import com.accor.wcp.console.services.sqsdlq.dynamo.DynamoDbPrimaryKey;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class SqsDocumentV2 implements Serializable, DynamoDbPrimaryKey {
  @Serial private static final long serialVersionUID = 1L;

  public static final String ATTRIBUTE_NAME_PARTITION_KEY = "partitionKey";
  public static final String ATTRIBUTE_NAME_ID = "id";
  public static final String ATTRIBUTE_NAME_BODY = "body";
  public static final String ATTRIBUTE_NAME_SENT_TIME = "sentTimestamp";
  public static final String ATTRIBUTE_NAME_QUEUE_NAME = "queueName";
  public static final String ATTRIBUTE_NAME_ATTRIBUTES = "attributes";
  public static final String ATTRIBUTE_STATUS_ATTRIBUTES = "status";
  public static final String ATTRIBUTE_NAME_GROUP_ID = "groupId";
  public static final String ATTRIBUTE_ENVIRONMENT = "environment";
  public static final String ATTRIBUTE_DOMAIN = "domain";

  // Format: <domain_env_queueName>
  private String partitionKey;
  // Format: <unixmillisecondtimestamp_UUID> (given by Apache Camel when document is created)
  private String id;
  // TTL
  private Long evictionDateTime;
  private String body;
  private Long sentTimestamp;
  private String groupId;
  @Builder.Default private List<AttributeDocument> attributes = new ArrayList<>();
  @Builder.Default private SqsDocumentStatus status = SqsDocumentStatus.NEW;
  private String comment;
  @Builder.Default private List<SqsDocumentRetry> retries = new ArrayList<>();

  @DynamoDbPartitionKey
  public String getPartitionKey() {
    return partitionKey;
  }

  @DynamoDbSortKey
  public String getId() {
    return id;
  }

  @DynamoDbIgnore
  @Override
  public Key getPrimaryKey() {
    return Key.builder().partitionValue(partitionKey).sortValue(id).build();
  }
}
