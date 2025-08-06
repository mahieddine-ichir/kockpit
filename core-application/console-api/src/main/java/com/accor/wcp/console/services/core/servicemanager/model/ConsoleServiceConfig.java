package com.accor.wcp.console.services.core.servicemanager.model;

import com.accor.wcp.console.sdk.service.WCPConsoleServiceConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ConsoleServiceConfig {
    private String serviceId;
    private WCPConsoleServiceConfig config;
}
