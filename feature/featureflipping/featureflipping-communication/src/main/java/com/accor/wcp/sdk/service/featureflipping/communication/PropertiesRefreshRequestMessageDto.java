package com.accor.wcp.sdk.service.featureflipping.communication;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertiesRefreshRequestMessageDto implements Serializable {
  @Builder.Default private String __type__ = PropertiesRefreshRequestMessageDto.class.getName();
}
