package com.accor.wcp.console.services.dynaconfig.instance.communication.dto;

import com.accor.wcp.sdk.service.dynaconfig.communication.PropertyUpdateMessageRequestDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DynaConfigRequest {
  private String requestId;
  private String domain;
  private String env;
  private String appId;
  private String instanceId;
  private Long timestamp;

  private List<PropertyUpdateMessageRequestDto> messages;
  private List<DynaConfigResponse> responses;
}
