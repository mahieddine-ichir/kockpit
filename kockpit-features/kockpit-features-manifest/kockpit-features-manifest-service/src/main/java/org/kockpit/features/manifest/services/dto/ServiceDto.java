package org.kockpit.features.manifest.services.dto;

import lombok.Data;

@Data
public class ServiceDto {

    private String type;

    private String name;

    private String id;

    private Object config;
}

