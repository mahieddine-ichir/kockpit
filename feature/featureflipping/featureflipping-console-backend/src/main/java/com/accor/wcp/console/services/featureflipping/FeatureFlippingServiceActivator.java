package com.accor.wcp.console.services.featureflipping;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import com.accor.wcp.console.sdk.service.ApplicationMessageNotification;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceActivator;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMenu;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMetadata;
import com.accor.wcp.console.sdk.service.impl.WCPConsoleServiceMenuDefault;
import com.accor.wcp.console.sdk.service.impl.WCPConsoleServiceMetadataDefault;
import com.accor.wcp.console.services.featureflipping.dynamo.FeatureFlippingDocumentService;
import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingDocument;
import com.accor.wcp.console.services.featureflipping.instance.FeatureFlippingSynchronizeInstanceService;
import com.accor.wcp.console.services.featureflipping.instance.communication.FeatureFlippingInstanceResponseHandler;
import com.accor.wcp.console.services.featureflipping.manifest.FeatureFlippingPropertySettings;
import com.accor.wcp.console.services.featureflipping.manifest.FeatureFlippingServiceManifest;
import com.accor.wcp.console.services.featureflipping.manifest.FeatureFlippingSettingsDto;
import com.accor.wcp.sdk.command.manager.CommandExecutor;
import com.accor.wcp.sdk.command.manager.CommandManager;
import com.accor.wcp.sdk.service.featureflipping.communication.InstanceInitPropertiesMessageDto;
import com.accor.wcp.sdk.service.featureflipping.communication.InstanceInitPropertiesUpdateResponseDto;
import com.accor.wcp.sdk.service.featureflipping.communication.PropertyUpdateMessageResponseDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.accor.wcp.sdk.service.featureflipping.ServiceDefinition.SERVICE_ID;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeatureFlippingServiceActivator implements WCPConsoleServiceActivator {

  private final FeatureFlippingInstanceResponseHandler featureFlippingInstanceResponseHandler;
  private final FeatureFlippingSynchronizeInstanceService featureFlippingSynchronizeInstanceService;
  private final FeatureFlippingDocumentService featureFlippingDocumentService;

  private ObjectMapper objectMapper;
  private CommandManager<ApplicationMessageNotification> commandManager;

  @Getter
  private FeatureFlippingServiceManifest serviceManifest;

  @PostConstruct
  void init() {
      objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
      objectMapper.registerModule(new JavaTimeModule());

    Map<String, CommandExecutor<ApplicationMessageNotification>> commandsMap = new HashMap<>();
    commandsMap.put(
        InstanceInitPropertiesMessageDto.class.getName(),
        notif -> featureFlippingInstanceResponseHandler.handleInstanceInitProperties(notif, serviceManifest));
    commandsMap.put(
        PropertyUpdateMessageResponseDto.class.getName(),
        featureFlippingInstanceResponseHandler::handleInstanceUpdateResponse);
    commandsMap.put(
        InstanceInitPropertiesUpdateResponseDto.class.getName(),
        featureFlippingInstanceResponseHandler::handleInstanceMultiUpdatesResponse);

    commandManager =
        new CommandManager<>(
            FeatureFlippingApplicationMessageNotificationCommandTypeAccessor.create(), commandsMap);
  }

  @Override
  public String getServiceId() {
    return SERVICE_ID;
  }

  @Override
  public WCPConsoleServiceMetadata load(Collection<? extends AppManifest> appManifests) {
    List<WCPConsoleServiceMenu> menus = new ArrayList<>();
    Map<String, FeatureFlippingSettingsDto> settingsMap = new HashMap<>();

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
                  FeatureFlippingSettingsDto settings = this.buildSettings(domain, env, appId, e);
                  if (isNull(settings)) {
                      return;
                  }
                  settings.setEnv(env);
                  if (! CollectionUtils.isEmpty(settings.getKeys())) {
                      this.synchronizeSettings(domain, env, appId, settings);
                  }

                  String name = domain + "-" + settings.getEnv() + "-" + appId;
                  settingsMap.put(name, settings);

                  WCPConsoleServiceMenuDefault menu =
                      WCPConsoleServiceMenuDefault.builder()
                          .id(name)
                          .label(settings.getLabel())
                          .route("/services/" + SERVICE_ID + "/" + domain + "/" + env + "/" + appId)
                          .env(env)
                          .domain(domain)
                          .subDomain(subDomain)
                          .app(appId)
                          .build();
                  menus.add(menu);
              });
          this.featureFlippingSynchronizeInstanceService.refreshInstances(domain, env, appId);
        });

    serviceManifest =
        FeatureFlippingServiceManifest.builder().featureFlippingSettingsMap(settingsMap).build();

    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    executorService.schedule(
        () -> this.sendRefreshInstanceRequest(appManifests), 5, TimeUnit.MINUTES);

    return WCPConsoleServiceMetadataDefault.builder().menus(menus).config(serviceManifest).build();
  }

  private FeatureFlippingSettingsDto buildSettings(
      String domain, String env, String appId, Map<String, Object> props) {
    try {
      return objectMapper.convertValue(props, FeatureFlippingSettingsDto.class);
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

  private void sendRefreshInstanceRequest(Collection<? extends AppManifest> appManifests) {
    appManifests.forEach(
        appManifest -> {
          if (nonNull(appManifest.getServiceData(getServiceId()))) {
            String applicationId = appManifest.getApplicationId();
            this.featureFlippingSynchronizeInstanceService.refreshInstances(
                appManifest.getDomain(), appManifest.getEnv(), applicationId);
          }
        });
  }

  private void synchronizeSettings(String domain, String env, String appId, FeatureFlippingSettingsDto featureFlippingSettings) {
      List<FeatureFlippingDocument.PropertyDocument> featureFlippingProperties = this.featureFlippingDocumentService.getFeatureFlippingProperties(domain, env, appId);
      // delete from repo, those not in present in manifest
      this.filterPropertiesDefinedInRepoButNotInManifestSettings(featureFlippingProperties, featureFlippingSettings)
              .forEach(propertyDocument -> featureFlippingDocumentService.removeDocumentProperty(domain, env, appId, propertyDocument.getKey()));
  }

  // Left outer join
  private List<FeatureFlippingDocument.PropertyDocument> filterPropertiesDefinedInRepoButNotInManifestSettings(List<FeatureFlippingDocument.PropertyDocument> repoProperties, FeatureFlippingSettingsDto settingsProperties) {
      return repoProperties.stream().filter(propertyDocument ->
                      settingsProperties.getKeys().stream().map(FeatureFlippingPropertySettings::getKey)
                              .noneMatch(key -> key.equals(propertyDocument.getKey()))
              ).toList();
  }
}
