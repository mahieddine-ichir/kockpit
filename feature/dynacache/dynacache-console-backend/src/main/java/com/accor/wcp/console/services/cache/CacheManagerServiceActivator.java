package com.accor.wcp.console.services.cache;

import static java.util.Objects.isNull;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import com.accor.wcp.console.sdk.service.ApplicationMessageNotification;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceActivator;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMenu;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMetadata;
import com.accor.wcp.console.sdk.service.impl.WCPConsoleServiceMenuDefault;
import com.accor.wcp.console.sdk.service.impl.WCPConsoleServiceMetadataDefault;
import com.accor.wcp.sdk.command.manager.CommandExecutor;
import com.accor.wcp.sdk.command.manager.CommandManager;
import com.accor.wcp.sdk.service.cache.communication.CacheInstanceOperationResult;
import com.accor.wcp.sdk.service.cache.communication.CacheOperationResult;
import com.accor.wcp.sdk.service.cache.communication.CacheReloadMessageResponseDto;
import com.accor.wcp.sdk.service.cache.communication.CacheResetStatsMessageResponseDto;
import com.accor.wcp.sdk.service.cache.communication.CacheStatisticsMessage;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheManagerServiceActivator implements WCPConsoleServiceActivator {

  private final StateManager stateManager;
  private final ObjectMapper objectMapper;
  private final HistoCommandManager histoCommandManager;
  private CommandManager<ApplicationMessageNotification> commandManager;

  public CacheManagerServiceActivator(
      StateManager stateManager, HistoCommandManager histoCommandManager) {
    this.stateManager = stateManager;
    this.histoCommandManager = histoCommandManager;
    objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
  }

  @PostConstruct
  void init() {
    Map<String, CommandExecutor<ApplicationMessageNotification>> commandsMap = new HashMap<>();
    CommandExecutor<ApplicationMessageNotification> responseReloadNotification =
        this::responseReloadNotification;
    commandsMap.put(CacheReloadMessageResponseDto.class.getName(), responseReloadNotification);

    CommandExecutor<ApplicationMessageNotification> handleCacheStatisticsNotification =
        this::handleCacheStatisticsNotification;
    commandsMap.put(CacheStatisticsMessage.class.getName(), handleCacheStatisticsNotification);

    CommandExecutor<ApplicationMessageNotification> handleRefreshCacheStatsNotification =
        this::handleRefreshCacheStatsNotification;
    commandsMap.put(
        CacheResetStatsMessageResponseDto.class.getName(), handleRefreshCacheStatsNotification);

    commandManager =
        new CommandManager<>(
            CacheApplicationMessageNotificationCommandTypeAccessor.create(), commandsMap);
  }

  private void handleRefreshCacheStatsNotification(ApplicationMessageNotification notification) {

    CacheResetStatsMessageResponseDto message =
        getMessage(notification, CacheResetStatsMessageResponseDto.class);

    log.info("Treat cache reload response {}", message);
    if (!CacheOperationResult.DONE.equals(message.getResult())) {
      log.warn("Error reloading cache: {} - {}", message.getCache(), notification);
    }
    histoCommandManager.upsertCacheInstanceOperationResult(
        notification.getDomain(),
        notification.getEnv(),
        notification.getApplicationId(),
        message.getCache(),
        notification.getRequestMessageId(),
        notification.getInstanceId(),
        CacheInstanceOperationResult.builder()
            .dateTime(Instant.now().toEpochMilli())
            .result(message.getResult())
            .build());
  }

  private void handleCacheStatisticsNotification(
      ApplicationMessageNotification applicationMessageNotification) {
    stateManager.updateCacheState(
        applicationMessageNotification.getEnv(),
        applicationMessageNotification.getDomain(),
        applicationMessageNotification.getApplicationId(),
        applicationMessageNotification.getInstanceId(),
        applicationMessageNotification.getMessage());
  }

  @Override
  public String getServiceId() {
    return "cache";
  }

  @Override
  public WCPConsoleServiceMetadata load(Collection<? extends AppManifest> appManifests) {
    List<WCPConsoleServiceMenu> menus = new ArrayList<>();
    Map<String, CacheSettingsDto> cacheSettingsMap = new HashMap<>();

    // Iterate on application
    appManifests.forEach(
        appManifest -> {
          // All Caches for this application
          List<Map<String, Object>> serviceDataList = appManifest.getServiceData(getServiceId());
          if (isNull(serviceDataList)) {
            return;
          }
          String env = appManifest.getEnv();
          String domain = appManifest.getDomain();
          String subDomain = appManifest.getSubDomain();
          String appId = appManifest.getApplicationId();

          serviceDataList.forEach(
              e -> {
                CacheSettingsDto cacheSettings = this.buildCacheSettingsDto(domain, env, appId, e);
                if (Objects.nonNull(cacheSettings)) {
                  cacheSettings.setEnv(env);
                  String name = cacheSettings.getName();
                  cacheSettingsMap.put(name, cacheSettings);
                  WCPConsoleServiceMenuDefault menu =
                      WCPConsoleServiceMenuDefault.builder()
                          .id(name)
                          .label(cacheSettings.getLabel())
                          .route("/services/cache/" + domain + "/" + env + "/" + appId + "/" + name)
                          .env(env)
                          .domain(domain)
                          .subDomain(subDomain)
                          .app(appId)
                          .build();
                  menus.add(menu);
                }
              });
        });

    CacheServiceManifest serviceManifest =
        CacheServiceManifest.builder().cacheSettingsMap(cacheSettingsMap).build();

    return WCPConsoleServiceMetadataDefault.builder().menus(menus).config(serviceManifest).build();
  }

  @Override
  public void notification(ApplicationMessageNotification notification) {
    log.debug("Notification from application for cache: {}", notification);
    commandManager.execute(notification);
  }

  private void responseReloadNotification(ApplicationMessageNotification notification) {
    CacheReloadMessageResponseDto message =
        getMessage(notification, CacheReloadMessageResponseDto.class);

    log.info("Treat cache reload response {}", message);
    if (!CacheOperationResult.DONE.equals(message.getResult())) {
      log.warn("Error reloading cache: {} - {}", message.getCache(), notification);
    }
    histoCommandManager.upsertCacheInstanceOperationResult(
        notification.getDomain(),
        notification.getEnv(),
        notification.getApplicationId(),
        message.getCache(),
        notification.getRequestMessageId(),
        notification.getInstanceId(),
        CacheInstanceOperationResult.builder()
            .dateTime(Instant.now().toEpochMilli())
            .result(message.getResult())
            .build());
  }

  private <T> T getMessage(ApplicationMessageNotification notification, Class<T> type) {
    return objectMapper.convertValue(notification.getMessage(), type);
  }

  @Override
  public WCPConsoleServiceMetadata reload(Collection<? extends AppManifest> appManifests) {
    return load(appManifests);
  }

  private CacheSettingsDto buildCacheSettingsDto(
      String domain, String env, String appId, Map<String, Object> props) {
    try {
      return objectMapper.convertValue(props, CacheSettingsDto.class);
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
}
