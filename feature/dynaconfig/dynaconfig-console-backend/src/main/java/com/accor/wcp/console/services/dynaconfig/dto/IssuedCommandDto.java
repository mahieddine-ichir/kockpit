package com.accor.wcp.console.services.dynaconfig.dto;

import com.accor.wcp.sdk.service.dynaconfig.communication.DynaConfigOperationResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class IssuedCommandDto {
  private CommandTypeDto type;
  private Long timestamp;
  private String requestId;
  private String applicationInstance;
  private String propertyName;
  private DynaConfigOperationResult status;
}
