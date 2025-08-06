package com.accor.wcp.console.services.sqsdlq.model;

import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsAttributeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SqsAttributeDto {

  private String name;
  private SqsAttributeType type;
  private String value;
}
