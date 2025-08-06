package com.accor.wcp.console.services.darkcanary;

import com.accor.wcp.console.sdk.service.WCPConsoleServiceConfig;
import com.accor.wcp.console.services.darkcanary.model.DarkCanaryConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
class DarkCanaryServiceManifest implements WCPConsoleServiceConfig {
  private List<DarkCanaryConfiguration> darkCanaryConfigurations;
}
