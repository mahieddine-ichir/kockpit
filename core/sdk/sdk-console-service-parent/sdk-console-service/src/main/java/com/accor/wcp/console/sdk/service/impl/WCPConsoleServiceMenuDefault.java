package com.accor.wcp.console.sdk.service.impl;

import com.accor.wcp.console.sdk.service.WCPConsoleServiceMenu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/** Default {@link WCPConsoleServiceMenu} implementation. */
@Data
@Builder
@AllArgsConstructor
public class WCPConsoleServiceMenuDefault implements WCPConsoleServiceMenu {
  private String parentId;
  private String id;
  private String label;
  private String route;
  private String domain;
  private String subDomain;
  private String env;
  private String app;
}
