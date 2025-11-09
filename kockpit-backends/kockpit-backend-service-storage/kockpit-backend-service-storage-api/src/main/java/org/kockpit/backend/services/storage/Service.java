package org.kockpit.backend.services.storage;

import lombok.Data;

@Data
public class Service {

  private String name;

  private String appId;

  private Object config;
}

