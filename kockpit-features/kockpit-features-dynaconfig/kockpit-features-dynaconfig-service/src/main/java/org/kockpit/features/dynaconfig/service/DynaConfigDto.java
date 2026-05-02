package org.kockpit.features.dynaconfig.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DynaConfigDto {

    private String domain;

    private String env;

    private String appId;

    private String key;

    private String comment;

    private Object value;
}
