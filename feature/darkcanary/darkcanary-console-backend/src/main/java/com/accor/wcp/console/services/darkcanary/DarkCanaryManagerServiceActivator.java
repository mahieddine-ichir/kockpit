package com.accor.wcp.console.services.darkcanary;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import com.accor.wcp.console.sdk.notification.WCPConsoleUserNotificationService;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceActivator;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMenu;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMetadata;
import com.accor.wcp.console.sdk.service.impl.WCPConsoleServiceMenuDefault;
import com.accor.wcp.console.sdk.service.impl.WCPConsoleServiceMetadataDefault;
import com.accor.wcp.console.services.darkcanary.builder.WCPConsoleServiceMenuBuilder;
import com.accor.wcp.console.services.darkcanary.config.DarkCanaryApplicationProperties;
import com.accor.wcp.console.services.darkcanary.model.DarkCanaryConfiguration;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component
@Slf4j
public class DarkCanaryManagerServiceActivator implements WCPConsoleServiceActivator {

  public static final String SERVICE_ID = "darkcanary";

  private final ObjectMapper mapper;

  @Getter
  private DarkCanaryServiceManifest darkCanaryServiceManifest;

  private final DarkCanaryApplicationProperties properties;
  private final WCPConsoleUserNotificationService userNotificationService;
  private final Map<String, String> notifications = new HashMap<>();

  public DarkCanaryManagerServiceActivator(
      DarkCanaryApplicationProperties properties,
      WCPConsoleUserNotificationService userNotificationService) {
    this.properties = properties;
    this.userNotificationService = userNotificationService;
    mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//    mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
  }

  @Override
  public String getServiceId() {
    return SERVICE_ID;
  }

  @Override
  public WCPConsoleServiceMetadata load(Collection<? extends AppManifest> appManifests) {
    List<WCPConsoleServiceMenu> menus = new ArrayList<>();
    List<DarkCanaryConfiguration> darkCanaryConfigurations = new ArrayList<>();

    // Iterate on application
    appManifests.forEach(
        appManifest -> {
          // All SQS/DLQ for this application
          List<Map<String, Object>> serviceDataList = appManifest.getServiceData(getServiceId());
          if (isNull(serviceDataList)) {
            return;
          }
          String env = appManifest.getEnv();
          String domain = appManifest.getDomain();
          String applicationId = appManifest.getApplicationId();

          serviceDataList.forEach(
              serviceProps -> {
                DarkCanaryConfiguration configuration =
                    this.buildSettings(domain, env, applicationId, serviceProps);

                configuration.setDomain(domain);

                darkCanaryConfigurations.add(configuration);
                menus.add(buildMenuItem(appManifest, configuration));
              });
        });

    darkCanaryServiceManifest =
        DarkCanaryServiceManifest.builder().darkCanaryConfigurations(darkCanaryConfigurations).build();

    return WCPConsoleServiceMetadataDefault.builder()
        .menus(menus)
        .config(darkCanaryServiceManifest)
        .build();
  }

  private DarkCanaryConfiguration buildSettings(
      String domain, String env, String appId, Map<String, Object> props) {
    try {
      return mapper.convertValue(props, DarkCanaryConfiguration.class);
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

  WCPConsoleServiceMenuDefault buildMenuItem(AppManifest manifest, DarkCanaryConfiguration settings) {
    return new WCPConsoleServiceMenuBuilder()
        .withAppManifest(manifest)
        .withSettings(settings)
        .build();
  }

  @Override
  public WCPConsoleServiceMetadata reload(Collection<? extends AppManifest> appManifests) {
    unload();
    return load(appManifests);
  }

  private void unload() {
    // Nothing to unload for the moment
  }
}
