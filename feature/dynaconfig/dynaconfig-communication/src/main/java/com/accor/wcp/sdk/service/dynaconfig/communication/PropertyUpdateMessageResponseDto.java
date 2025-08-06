package com.accor.wcp.sdk.service.dynaconfig.communication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyUpdateMessageResponseDto implements Serializable {
  @Builder.Default private String __type__ = PropertyUpdateMessageResponseDto.class.getName();
  private String propertyName;
  private DynaConfigOperationResult result;
  private String errorMessage;
}
