package org.kockpit.communication.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PropertyUpdateMessageRequestDto {

    @JsonProperty("__type__")
    private String type = "com.accor.wcp.sdk.service.dynaconfig.communication.PropertyUpdateMessageRequestDto";

    private String propertyName;
    private Object newValue;
}
