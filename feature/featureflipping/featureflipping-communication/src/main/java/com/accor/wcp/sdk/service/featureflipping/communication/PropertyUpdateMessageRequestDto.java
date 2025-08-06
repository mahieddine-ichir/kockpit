package com.accor.wcp.sdk.service.featureflipping.communication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyUpdateMessageRequestDto implements Serializable {
  @Builder.Default private String __type__ = PropertyUpdateMessageRequestDto.class.getName();
  private String propertyName;
  private String newValue;
}
