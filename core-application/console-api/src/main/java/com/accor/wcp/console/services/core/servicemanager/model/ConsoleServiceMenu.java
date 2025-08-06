package com.accor.wcp.console.services.core.servicemanager.model;

import com.accor.wcp.console.sdk.service.WCPConsoleServiceMenu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ConsoleServiceMenu {
    private String serviceId;
    private List<WCPConsoleServiceMenu> menuItems;
}
