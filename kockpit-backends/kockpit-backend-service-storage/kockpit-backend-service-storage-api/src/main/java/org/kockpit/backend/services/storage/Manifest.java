package org.kockpit.backend.services.storage;

import lombok.Data;

import java.util.List;

@Data
public class Manifest {

    private String name;

    private String appId;

    private List<ConfigItem> configs;
}
