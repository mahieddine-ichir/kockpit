package org.kockpit.communication.legacy.featureflipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeatureFlippingPropertyUpdateRequestDto {

    @JsonProperty("__type__")
    @Builder.Default
    private String type = "com.accor.wcp.sdk.service.featureflipping.communication.PropertyUpdateMessageRequestDto";

    private String propertyName;
    private Object newValue;
}