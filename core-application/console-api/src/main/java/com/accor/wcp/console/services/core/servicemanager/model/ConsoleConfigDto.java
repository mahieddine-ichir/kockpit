package com.accor.wcp.console.services.core.servicemanager.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ConsoleConfigDto {
  private List<ConsoleServiceMenu> consoleServiceMenus;

  private List<ConsoleServiceConfig> consoleServiceConfigs;
}
