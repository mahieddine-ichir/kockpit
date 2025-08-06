package com.accor.wcp.console.services.dynaconfig.instance.communication.dto;

import com.accor.wcp.sdk.service.dynaconfig.communication.PropertyUpdateMessageResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DynaConfigResponse {
  private String requestId;
  private Long timestamp;
  private String instanceId;
  private PropertyUpdateMessageResponseDto message;
}
