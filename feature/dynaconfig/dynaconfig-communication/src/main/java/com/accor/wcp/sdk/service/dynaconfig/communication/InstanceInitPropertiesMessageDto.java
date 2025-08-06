package com.accor.wcp.sdk.service.dynaconfig.communication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstanceInitPropertiesMessageDto implements Serializable {
    @Builder.Default private String __type__ = InstanceInitPropertiesMessageDto.class.getName();
    private Map<String, Set<Object>> propertyValues;
}
