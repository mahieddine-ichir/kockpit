package org.kockpit.communication.legacy.dynaconfig;

import com.fasterxml.jackson.annotation.JsonProperty;
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

  @JsonProperty("__type__")
  private String type = "com.accor.wcp.sdk.service.dynaconfig.communication.PropertyUpdateMessageResponseDto";

  private String propertyName;

  private DynaConfigOperationResult result;
  private String errorMessage;
}
