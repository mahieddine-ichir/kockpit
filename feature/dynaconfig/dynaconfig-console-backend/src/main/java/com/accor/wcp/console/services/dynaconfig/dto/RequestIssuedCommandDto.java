package com.accor.wcp.console.services.dynaconfig.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RequestIssuedCommandDto extends IssuedCommandDto {
  private String propertyValue;
}
