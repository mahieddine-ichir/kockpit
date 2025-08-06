package com.accor.wcp.console.services.sqsdlq.dynamo;

import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;

public interface DynamoDbPrimaryKey {
  @DynamoDbIgnore
  Key getPrimaryKey();
}
