package org.kockpit.communication.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstanceInitPropertiesUpdateRequestDto {

    @JsonProperty("__type__")
    private String type = "com.accor.wcp.sdk.service.dynaconfig.communication.InstanceInitPropertiesUpdateRequestDto";

    private List<PropertyUpdateMessageRequestDto> updates;
}
