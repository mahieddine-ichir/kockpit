package com.accor.wcp.console.services.featureflipping.dynamo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class FeatureFlippingDocument implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * Domain-Env-ApplicationId
   */
  private String id;
  private Long lastUpdatedTimestamp;
  private Map<String, PropertyDocument> propertyValues;

  @DynamoDbPartitionKey
  public String getId() {
    return this.id;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @DynamoDbBean
  public static class PropertyDocument implements Serializable {
    private static final long serialVersionUID = 1L;
    private String key;
    private String value;
    private Long lastUpdatedTimestamp;
    private String comment;
    private LocalDate expiration;
    private List<PropertyHistoryDocument> history = new ArrayList<>();
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @DynamoDbBean
  public static class PropertyHistoryDocument implements Serializable {
    private Long timestamp;
    private String valueBeforeChange;
    private String valueAfterChange;
    private String username;
  }

}
