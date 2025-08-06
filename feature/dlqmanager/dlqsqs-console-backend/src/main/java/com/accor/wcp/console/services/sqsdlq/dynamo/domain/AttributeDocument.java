package com.accor.wcp.console.services.sqsdlq.dynamo.domain;

import java.io.Serial;
import java.io.Serializable;
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
public class AttributeDocument implements Serializable {

  public static final String ATTRIBUTE_NAME = "name";
  public static final String ATTRIBUTE_TYPE = "type";
  public static final String ATTRIBUTE_VALUE = "value";

  @Serial private static final long serialVersionUID = 1L;
  private String name;
  private SqsAttributeType type;
  private String value;
}
