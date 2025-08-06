package com.accor.wcp.console.services.core.servicemanager;

import com.accor.wcp.console.sdk.service.WCPConsoleServiceMenu;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMetadata;
import com.accor.wcp.console.services.core.servicemanager.model.ConsoleConfigDto;
import com.accor.wcp.console.services.core.servicemanager.model.ConsoleServiceConfig;
import com.accor.wcp.console.services.core.servicemanager.model.ConsoleServiceMenu;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.accor.wcp.console.services.core.security.AuthenticationHelper.getAuthenticatedUserGroups;
import static com.accor.wcp.console.services.core.security.AuthenticationHelper.isUserGroupsAuthorizedFor;

@RestController
@RequiredArgsConstructor
class ConsoleConfigApi {

  private final ServiceManager serviceManager;

  @GetMapping("/api/console/config")
  public ConsoleConfigDto get() {
    Map<String, WCPConsoleServiceMetadata> serviceMetadataMap =
        serviceManager.getServiceMetadataMap();
    List<ConsoleServiceMenu> consoleServiceMenus =
        serviceMetadataMap.entrySet().stream()
            .map(this::createMenu)
            .toList();
    List<ConsoleServiceConfig> consoleServiceConfigs =
        serviceMetadataMap.entrySet().stream()
            .map(
                e ->
                    ConsoleServiceConfig.builder()
                        .serviceId(e.getKey())
                        .config(e.getValue().getConfig())
                        .build())
            .toList();

    return ConsoleConfigDto.builder()
        .consoleServiceMenus(consoleServiceMenus)
        .consoleServiceConfigs(consoleServiceConfigs)
        .build();
  }

  private ConsoleServiceMenu createMenu(Map.Entry<String, WCPConsoleServiceMetadata> e) {
    List<String> groups = getAuthenticatedUserGroups();

    List<WCPConsoleServiceMenu> menus = e.getValue().getMenus();

      // Filter menus
    menus = menus.stream()
            .filter(m -> this.checkMenuAuthorization(m, groups))
            .toList();

      return ConsoleServiceMenu.builder()
              .serviceId(e.getKey())
              .menuItems(menus)
              .build();
  }

  private boolean checkMenuAuthorization(WCPConsoleServiceMenu menu, List<String> userGroups) {
    String domain = menu.getDomain();
    String env = menu.getEnv();
    return isUserGroupsAuthorizedFor(serviceManager, userGroups, domain, env);
  }
}
