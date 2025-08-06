package com.accor.wcp.sdk.service.dynaconfig.communication;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstanceInitPropertiesUpdateResponseDto implements Serializable {
  @Builder.Default private String __type__ = InstanceInitPropertiesUpdateResponseDto.class.getName();
  private List<PropertyUpdateMessageResponseDto> results;
}
