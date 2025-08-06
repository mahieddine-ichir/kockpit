package com.accor.wcp.console.services.featureflipping.instance.communication.dto;

import com.accor.wcp.sdk.service.featureflipping.communication.PropertyUpdateMessageRequestDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeatureFlippingRequest {
  private String requestId;
  private String domain;
  private String env;
  private String appId;
  private String instanceId;
  private Long timestamp;

  private List<PropertyUpdateMessageRequestDto> messages;
  private List<FeatureFlippingResponse> responses;
}
