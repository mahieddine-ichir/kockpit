package com.accor.wcp.console.services.cache;

import com.accor.wcp.console.sdk.service.WCPConsoleServiceConfig;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CacheServiceManifest implements WCPConsoleServiceConfig {
  private Map<String, CacheSettingsDto> cacheSettingsMap;
}
