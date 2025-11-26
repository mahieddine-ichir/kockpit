package org.kockpit.backend.services.storage;

import lombok.Data;

@Data
public class Service {

    private String type;

    private String name;

    private String id;

    private Object config;
}

