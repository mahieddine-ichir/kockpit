package com.accor.wcp.console.services.sqsdlq.dynamo.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class SqsDocument implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  public static final String ATTRIBUTE_NAME_ID = "id";
  public static final String ATTRIBUTE_NAME_BODY = "body";
  public static final String ATTRIBUTE_NAME_SENT_TIME = "sentTimestamp";
  public static final String ATTRIBUTE_NAME_QUEUE_NAME = "queueName";
  public static final String ATTRIBUTE_NAME_ATTRIBUTES = "attributes";
  public static final String ATTRIBUTE_STATUS_ATTRIBUTES = "status";
  public static final String ATTRIBUTE_NAME_GROUP_ID = "groupId";
  public static final String ATTRIBUTE_ENVIRONMENT = "environment";
  public static final String ATTRIBUTE_DOMAIN = "domain";

  private String id;
  private Long evictionDateTime;

  private String body;
  private Long sentTimestamp;
  private String queueName;
  private String groupId;
  private String environment;
  private String domain;
  private List<AttributeDocument> attributes = new ArrayList<>();
  private SqsDocumentStatus status = SqsDocumentStatus.NEW;
  private String comment;
  private List<SqsDocumentRetry> retries = new ArrayList<>();

  @DynamoDbPartitionKey
  public String getId() {
    return this.id;
  }
}
