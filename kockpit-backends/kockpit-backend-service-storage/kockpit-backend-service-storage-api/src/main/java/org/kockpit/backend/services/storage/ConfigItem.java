package org.kockpit.backend.services.storage;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonTypeName("config_item")
public class ConfigItem {

  private String domain;

  private String env;

  private List<Service> services = new ArrayList<>();
}

