package com.accor.wcp.console.services.audit.console.backend;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import com.accor.wcp.console.sdk.service.ApplicationMessageNotification;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceActivator;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMenu;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMetadata;
import com.accor.wcp.console.sdk.service.impl.WCPConsoleServiceMenuDefault;
import com.accor.wcp.console.sdk.service.impl.WCPConsoleServiceMetadataDefault;
import com.accor.wcp.console.services.audit.console.backend.search.dto.AuditViewDto;
import com.accor.wcp.console.services.audit.kengine.KEngineRegistryWriteRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class AuditServiceActivator implements WCPConsoleServiceActivator {

  private final KEngineRegistryWriteRepository kEngineRegistryWriteRepository;
  private final ObjectMapper objectMapper;

  @Getter
  private AuditServiceManifest auditServiceManifest;

  public AuditServiceActivator(KEngineRegistryWriteRepository kEngineRegistryWriteRepository) {
    this.kEngineRegistryWriteRepository = kEngineRegistryWriteRepository;
    objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    objectMapper.configure(
        DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
  }

  @Override
  public String getServiceId() {
    return "audit";
  }

  @Override
  public WCPConsoleServiceMetadata load(Collection<? extends AppManifest> appManifests) {
    List<AuditViewDto> auditViews = new ArrayList<>();
    List<WCPConsoleServiceMenu> menus = new ArrayList<>();

    // Iterate on application
    appManifests.forEach(
        appManifest -> {
          // All audit for this application
          List<Map<String, Object>> serviceDataList = appManifest.getServiceData(getServiceId());
          String domain = appManifest.getDomain();
          String subDomain = appManifest.getSubDomain();
          String env = appManifest.getEnv();
          String applicationId = appManifest.getApplicationId();

          serviceDataList.forEach(
              e -> {
                AuditViewDto auditViewDto = this.buildAuditViewDto(domain, env, applicationId, e);
                if (Objects.nonNull(auditViewDto)) {
                  auditViewDto.setDomain(domain);
                  auditViewDto.setEnv(env);

                  String name = auditViewDto.getName();
                  auditViews.add(auditViewDto);
                  WCPConsoleServiceMenuDefault menu =
                      WCPConsoleServiceMenuDefault.builder()
                          .id(name)
                          .label(auditViewDto.getLabel())
                          .route("/services/audit/" + domain + "/" + env + "/" + name)
                          .env(env)
                          .domain(domain)
                          .subDomain(subDomain)
                          .app(applicationId)
                          .build();
                  menus.add(menu);
                }
              });
        });

    auditServiceManifest = AuditServiceManifest.builder().auditViews(auditViews).build();

    return WCPConsoleServiceMetadataDefault.builder()
        .menus(menus)
        .config(auditServiceManifest)
        .build();
  }

  private AuditViewDto buildAuditViewDto(
      String domain, String env, String appId, Map<String, Object> props) {
    try {
      return objectMapper.convertValue(props, AuditViewDto.class);
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
  public WCPConsoleServiceMetadata reload(Collection<? extends AppManifest> appManifests) {
    return load(appManifests);
  }

  @Override
  public void notification(ApplicationMessageNotification notification) {
    // For the moment only use for KEngine Flows Referential management (INSERT)
    Map<String, Object> registry = (Map<String, Object>) notification.getMessage().get("registry");
    Map<String, Object> referential =
        (Map<String, Object>) notification.getMessage().get("referential");
    String referentialId =
        kEngineRegistryWriteRepository.getReferentialId(
            notification.getDomain(),
            notification.getEnv(),
            notification.getApplicationId(),
            referential,
            registry);
    log.info(
        "referentialId: {} and incoming registryId: {}",
        referentialId,
        notification.getMessage().get("registryId"));
  }
}
