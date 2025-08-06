package com.accor.wcp.console.sdk.service.impl;

import com.accor.wcp.console.sdk.service.WCPConsoleServiceConfig;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMenu;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMetadata;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/** Default {@link WCPConsoleServiceMetadata} implementation. */
@Data
@Builder
@AllArgsConstructor
public class WCPConsoleServiceMetadataDefault implements WCPConsoleServiceMetadata {
  private List<WCPConsoleServiceMenu> menus;
  private WCPConsoleServiceConfig config;
}
