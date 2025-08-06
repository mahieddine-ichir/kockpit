package com.accor.wcp.console.services.sqsdlq.dynamo.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class SqsDocumentRetry implements Serializable {
  public static final String ATTRIBUTE_NAME_BODY = "body";
  public static final String ATTRIBUTE_NAME_SENT_TIME = "sentTimestamp";
  public static final String ATTRIBUTE_NAME_ATTRIBUTES = "attributes";
  public static final String ATTRIBUTE_NAME_GROUP_ID = "groupId";

  private String body;
  private String sentTimestamp;
  private Long receiveTime;
  private String groupId;
  @Builder.Default private List<AttributeDocument> attributes = new ArrayList<>();
  private String status;
}
