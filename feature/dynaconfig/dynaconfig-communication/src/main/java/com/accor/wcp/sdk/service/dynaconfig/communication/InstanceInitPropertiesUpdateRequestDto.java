package com.accor.wcp.sdk.service.dynaconfig.communication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstanceInitPropertiesUpdateRequestDto implements Serializable {
  @Builder.Default private String __type__ = InstanceInitPropertiesUpdateRequestDto.class.getName();
  private List<PropertyUpdateMessageRequestDto> updates;
}
