package com.accor.wcp.console.services.audit.kengine.dynamodb;

import java.io.Serializable;
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
public class KEngineRegistryDocument implements Serializable {
  private static final long serialVersionUID = 1L;

  public static final String ATTRIBUTE_CREATION_TIMESTAMP = "creation_timestamp";
  public static final String ATTRIBUTE_NAME_ID = "id";

  private String id;
  private String jsonValue;
  private long creationTimestamp;

  @DynamoDbPartitionKey
  public String getId() {
    return this.id;
  }
}
