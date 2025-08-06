package com.accor.wcp.console.services.dynaconfig.manifest;

import java.util.List;
import lombok.Data;

@Data
public class DynaConfigSettingsDto {

  private String env;

  private String name;

  private String label;

  private ExecutionModeEnum executionMode;

  private List<DynaConfigPropertySettings> properties;
}
