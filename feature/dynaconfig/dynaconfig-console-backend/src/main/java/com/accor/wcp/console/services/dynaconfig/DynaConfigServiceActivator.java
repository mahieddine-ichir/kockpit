package com.accor.wcp.console.services.dynaconfig;

import static com.accor.wcp.sdk.service.dynaconfig.ServiceDefinition.SERVICE_ID;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import com.accor.wcp.console.sdk.service.ApplicationMessageNotification;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceActivator;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMenu;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMetadata;
import com.accor.wcp.console.sdk.service.impl.WCPConsoleServiceMenuDefault;
import com.accor.wcp.console.sdk.service.impl.WCPConsoleServiceMetadataDefault;
import com.accor.wcp.console.services.dynaconfig.instance.SynchronizeInstanceService;
import com.accor.wcp.console.services.dynaconfig.instance.communication.InstanceResponseHandler;
import com.accor.wcp.console.services.dynaconfig.manifest.DynaConfigServiceManifest;
import com.accor.wcp.console.services.dynaconfig.manifest.DynaConfigSettingsDto;
import com.accor.wcp.sdk.command.manager.CommandExecutor;
import com.accor.wcp.sdk.command.manager.CommandManager;
import com.accor.wcp.sdk.service.dynaconfig.communication.InstanceInitPropertiesMessageDto;
import com.accor.wcp.sdk.service.dynaconfig.communication.InstanceInitPropertiesUpdateResponseDto;
import com.accor.wcp.sdk.service.dynaconfig.communication.PropertyUpdateMessageResponseDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DynaConfigServiceActivator implements WCPConsoleServiceActivator {

  private final InstanceResponseHandler instanceResponseHandler;
  private final ObjectMapper objectMapper;
  private final SynchronizeInstanceService synchronizeInstanceService;
  private CommandManager<ApplicationMessageNotification> commandManager;
  private DynaConfigServiceManifest serviceManifest;

  public DynaConfigServiceActivator(
      InstanceResponseHandler instanceResponseHandler,
      SynchronizeInstanceService synchronizeInstanceService) {
    this.instanceResponseHandler = instanceResponseHandler;
    this.synchronizeInstanceService = synchronizeInstanceService;
    objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
  }

  @PostConstruct
  void init() {
    Map<String, CommandExecutor<ApplicationMessageNotification>> commandsMap = new HashMap<>();
    commandsMap.put(
        InstanceInitPropertiesMessageDto.class.getName(),
        notif -> instanceResponseHandler.handleInstanceInitProperties(notif, serviceManifest));
    commandsMap.put(
        PropertyUpdateMessageResponseDto.class.getName(),
        instanceResponseHandler::handleInstanceUpdateResponse);
    commandsMap.put(
        InstanceInitPropertiesUpdateResponseDto.class.getName(),
        instanceResponseHandler::handleInstanceMultiUpdatesResponse);

    commandManager =
        new CommandManager<>(
            DynaConfigApplicationMessageNotificationCommandTypeAccessor.create(), commandsMap);
  }

  @Override
  public String getServiceId() {
    return SERVICE_ID;
  }

  @Override
  public WCPConsoleServiceMetadata load(Collection<? extends AppManifest> appManifests) {
    List<WCPConsoleServiceMenu> menus = new ArrayList<>();
    Map<String, DynaConfigSettingsDto> settingsMap = new HashMap<>();

    // Iterate on application
    appManifests.forEach(
        appManifest -> {
          // All props for this application
          List<Map<String, Object>> serviceDataList = appManifest.getServiceData(getServiceId());
          if (isNull(serviceDataList)) {
            return;
          }
          String appId = appManifest.getApplicationId();
          String domain = appManifest.getDomain();
          String subDomain = appManifest.getSubDomain();
          String env = appManifest.getEnv();

          serviceDataList.forEach(
              e -> {
                DynaConfigSettingsDto settings = this.buildDynaConfigSetting(domain, env, appId, e);
                if (Objects.nonNull(settings)) {
                  settings.setEnv(env);
                  String name = domain + "-" + settings.getEnv() + "-" + appId;
                  settingsMap.put(name, settings);

                  WCPConsoleServiceMenuDefault menu =
                      WCPConsoleServiceMenuDefault.builder()
                          .id(name)
                          .label(settings.getLabel())
                          .route("/services/dynaconfig/" + domain + "/" + env + "/" + appId)
                          .env(env)
                          .domain(domain)
                          .subDomain(subDomain)
                          .app(appId)
                          .build();
                  menus.add(menu);
                }
              });
          this.synchronizeInstanceService.refreshInstances(domain, env, appId);
        });

    serviceManifest =
        DynaConfigServiceManifest.builder().dynaConfigSettingsMap(settingsMap).build();

    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    executorService.schedule(
        () -> this.sendRefreshInstanceRequest(appManifests), 5, TimeUnit.MINUTES);

    return WCPConsoleServiceMetadataDefault.builder().menus(menus).config(serviceManifest).build();
  }

  private DynaConfigSettingsDto buildDynaConfigSetting(
      String domain, String env, String appId, Map<String, Object> props) {
    try {
      return objectMapper.convertValue(props, DynaConfigSettingsDto.class);
    } catch (Exception e) {
      log.warn(
          "An error occurred while parsing manifest : service={} domain={} env={} appId={} with error={}",
          this.getServiceId(),
          domain,
          env,
          appId,
          e.toString());
      return null;
    }
  }

  @Override
  public void notification(ApplicationMessageNotification notification) {
    log.debug("Notification from application for dynaConfig: {}", notification);
    commandManager.execute(notification);
  }

  @Override
  public WCPConsoleServiceMetadata reload(Collection<? extends AppManifest> appManifests) {
    return load(appManifests);
  }

  public DynaConfigServiceManifest getServiceManifest() {
    return serviceManifest;
  }

  private void sendRefreshInstanceRequest(Collection<? extends AppManifest> appManifests) {
    appManifests.forEach(
        appManifest -> {
          if (nonNull(appManifest.getServiceData(getServiceId()))) {
            String applicationId = appManifest.getApplicationId();
            this.synchronizeInstanceService.refreshInstances(
                appManifest.getDomain(), appManifest.getEnv(), applicationId);
          }
        });
  }
}
