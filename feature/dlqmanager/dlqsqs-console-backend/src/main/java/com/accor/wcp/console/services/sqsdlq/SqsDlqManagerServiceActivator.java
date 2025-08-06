package com.accor.wcp.console.services.sqsdlq;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import com.accor.wcp.console.sdk.notification.NotificationLevelType;
import com.accor.wcp.console.sdk.notification.WCPConsoleUserNotificationService;
import com.accor.wcp.console.sdk.notification.impl.DefaultUserNotification;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceActivator;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMenu;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMetadata;
import com.accor.wcp.console.sdk.service.impl.WCPConsoleServiceMenuDefault;
import com.accor.wcp.console.sdk.service.impl.WCPConsoleServiceMetadataDefault;
import com.accor.wcp.console.services.sqsdlq.builder.WCPConsoleServiceMenuBuilder;
import com.accor.wcp.console.services.sqsdlq.config.ApplicationProperties;
import com.accor.wcp.console.services.sqsdlq.model.SqsDlqSettingsDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class SqsDlqManagerServiceActivator implements WCPConsoleServiceActivator {

  public static final String SERVICE_ID = "sqsdlq";

  private static final Integer DEFAULT_CONCURRENT_CONSUMERS = 1;

  private final CamelContext camelContext;
  private final DynamoDbProcessor dynamoDbProcessor;
  private final ObjectMapper mapper;
  private SqsDlqServiceManifest sqsServiceManifest;

  private SqsRouteBuilder sqsRouteBuilder;
  private final ApplicationProperties properties;
  private final WCPConsoleUserNotificationService userNotificationService;
  private Map<String, String> notifications = new HashMap<>();

  public SqsDlqManagerServiceActivator(
      CamelContext camelContext,
      DynamoDbProcessor dynamoDbProcessor,
      ApplicationProperties properties,
      WCPConsoleUserNotificationService userNotificationService) {
    this.camelContext = camelContext;
    this.dynamoDbProcessor = dynamoDbProcessor;
    this.properties = properties;
    this.userNotificationService = userNotificationService;
    mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
  }

  @Override
  public String getServiceId() {
    return SERVICE_ID;
  }

  @Override
  public WCPConsoleServiceMetadata load(Collection<? extends AppManifest> appManifests) {
    List<WCPConsoleServiceMenu> menus = new ArrayList<>();
    List<SqsDlqSettingsDto> sqsDlqSettingsDtos = new ArrayList<>();

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
          String subDomain = appManifest.getSubDomain();
          String applicationId = appManifest.getApplicationId();

          serviceDataList.forEach(
              serviceProps -> {
                SqsDlqSettingsDto settingsDto =
                    this.buildSettings(domain, env, applicationId, serviceProps);

                if (nonNull(settingsDto)) {
                  settingsDto.setDomain(domain);
                  settingsDto.setSubDomain(subDomain);
                  settingsDto.setEnv(env);
                  settingsDto.setRoute("sqsdlq-" + settingsDto.getName() + "-" + settingsDto.getDlqArn());
                  setDefaultValues(settingsDto);
                  if (containsRoute(sqsDlqSettingsDtos, settingsDto.getRoute())) {
                    sendNotification(settingsDto.getRoute(), "routeId");
                    log.warn("Duplicate routeId : " + settingsDto.getRoute() + " for sqsdlq configuration in { domain:" + settingsDto.getDomain() + ", env:" + settingsDto.getEnv() + " }");
                  } else if (containsDlqArn(sqsDlqSettingsDtos, settingsDto.getDlqArn())) {
                    sendNotification(settingsDto.getDlqArn(), "dlqArn");
                    log.warn("Duplicate dlqArn : " + settingsDto.getDlqArn() + " for sqsdlq configuration in { domain:" + settingsDto.getDomain() + ", env:" + settingsDto.getEnv() + " }");
                  } else {
                    sqsDlqSettingsDtos.add(settingsDto);
                    menus.add(buildMenuItem(appManifest, settingsDto));
                  }
                }
              });
        });

    sqsServiceManifest =
        SqsDlqServiceManifest.builder().sqsDlqSettingsDtos(sqsDlqSettingsDtos).build();

    // Camel routes
    try {
      sqsRouteBuilder = new SqsRouteBuilder(dynamoDbProcessor, sqsServiceManifest, properties);
      this.camelContext.addRoutes(sqsRouteBuilder);
    } catch (Exception e) {
      log.error(e.toString(), e);
    }

    return WCPConsoleServiceMetadataDefault.builder()
        .menus(menus)
        .config(sqsServiceManifest)
        .build();
  }

  private void sendNotification(String content, String param) {
    // Notification
    DefaultUserNotification notification =
        DefaultUserNotification.builder()
            .level(NotificationLevelType.WARNING)
            .applicationId("core")
            .description("Duplicate " + param + " value as : " + content + " in sqsdlq configuration")
            .date(new Date())
            .build();

    if (notifications.containsKey(content)) {
      userNotificationService.update(notifications.get(content), notification);
    } else {
      String notificationId = userNotificationService.create(SERVICE_ID, notification);
      notifications.put(content, notificationId);
    }
  }

  private boolean containsDlqArn(List<SqsDlqSettingsDto> sqsDlqSettingsDtos, String dlqArn) {
    return sqsDlqSettingsDtos.stream()
        .map(SqsDlqSettingsDto::getDlqArn).
        anyMatch(dlqArn::equals);
  }

  private boolean containsRoute(List<SqsDlqSettingsDto> sqsDlqSettingsDtos, String route) {
    return sqsDlqSettingsDtos.stream()
        .map(SqsDlqSettingsDto::getRoute).
        anyMatch(route::equals);
  }

  private void setDefaultValues(SqsDlqSettingsDto settingsDto) {
    settingsDto.setDeleteWhenReplay(TRUE.equals(settingsDto.getDeleteWhenReplay()));
    settingsDto.setConcurrentConsumers(
        Optional.ofNullable(settingsDto.getConcurrentConsumers())
            .orElse(DEFAULT_CONCURRENT_CONSUMERS));
  }

  private SqsDlqSettingsDto buildSettings(
      String domain, String env, String appId, Map<String, Object> props) {
    try {
      return mapper.convertValue(props, SqsDlqSettingsDto.class);
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

  WCPConsoleServiceMenuDefault buildMenuItem(AppManifest manifest, SqsDlqSettingsDto settings) {
    return new WCPConsoleServiceMenuBuilder()
        .withAppManifest(manifest)
        .withSettings(settings)
        .build();
  }

  SqsDlqServiceManifest getSqsServiceManifest() {
    return sqsServiceManifest;
  }

  @Override
  public WCPConsoleServiceMetadata reload(Collection<? extends AppManifest> appManifests) {
    unload();
    return load(appManifests);
  }

  private void unload() {
    if (isNull(sqsRouteBuilder)) {
      // Nothing to do
      return;
    }
    sqsRouteBuilder.destroy();
    sqsRouteBuilder = null;
  }
}
