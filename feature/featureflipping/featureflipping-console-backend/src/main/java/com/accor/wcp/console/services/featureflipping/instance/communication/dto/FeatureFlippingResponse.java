package com.accor.wcp.console.services.featureflipping.instance.communication.dto;

import com.accor.wcp.sdk.service.featureflipping.communication.PropertyUpdateMessageResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeatureFlippingResponse {
  private String requestId;
  private Long timestamp;
  private String instanceId;
  private PropertyUpdateMessageResponseDto message;
}
