package com.accor.wcp.console.services.dynaconfig.manifest;

import com.accor.wcp.console.sdk.service.WCPConsoleServiceConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class DynaConfigServiceManifest implements WCPConsoleServiceConfig {
  private Map<String, DynaConfigSettingsDto> dynaConfigSettingsMap;
}
