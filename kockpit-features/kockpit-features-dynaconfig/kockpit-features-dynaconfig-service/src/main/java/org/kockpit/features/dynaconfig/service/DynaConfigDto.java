package org.kockpit.features.dynaconfig.service;

import lombok.Data;

@Data
public class DynaConfigDto {

    private String key;

    private String comment;

    private Object value;
}
