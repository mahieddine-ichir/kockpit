package com.accor.wcp.console.services.sqsdlq;

import com.accor.wcp.console.sdk.service.WCPConsoleServiceConfig;
import com.accor.wcp.console.services.sqsdlq.model.SqsDlqSettingsDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
class SqsDlqServiceManifest implements WCPConsoleServiceConfig {
  private List<SqsDlqSettingsDto> sqsDlqSettingsDtos;
}
