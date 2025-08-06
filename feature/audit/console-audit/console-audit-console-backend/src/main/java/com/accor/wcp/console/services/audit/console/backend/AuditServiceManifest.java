package com.accor.wcp.console.services.audit.console.backend;

import com.accor.wcp.console.sdk.service.WCPConsoleServiceConfig;
import com.accor.wcp.console.services.audit.console.backend.search.dto.AuditViewDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuditServiceManifest implements WCPConsoleServiceConfig {
  public static final String CONFIG_NAME_DEFAULTS = "defaults";

  private List<AuditViewDto> auditViews;
}
