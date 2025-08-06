package com.accor.wcp.console.services.dynaconfig.dynamo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class DynaConfigHistoryDocument implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * Domain-Env-ApplicationId
   */
  private String id;
  private Long lastUpdatedTimestamp;
  private DynaConfigDocument.PropertyHistoryDocument historyPropertyValues;

  @DynamoDbPartitionKey
  public String getId() {
    return this.id;
  }
}
