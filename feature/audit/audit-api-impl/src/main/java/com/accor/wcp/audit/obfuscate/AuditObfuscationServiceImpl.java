package com.accor.wcp.audit.obfuscate;

import com.accor.wcp.audit.Audit;
import com.accor.wcp.audit.AuditEvent;
import com.accor.wcp.audit.AuditObfuscationService;
import com.accor.wcp.audit.AuditReport;
import com.accor.wcp.obfuscation.ObfuscateConfig;
import com.accor.wcp.obfuscation.ObfuscationService;
import com.accor.wcp.obfuscation.ObjectObfuscateConfig;
import com.accor.wcp.obfuscation.impl.ObjectObfuscateConfigImpl;
import com.accor.wcp.obfuscation.impl.obfuscators.json.JsonObfuscateConfig;
import com.accor.wcp.obfuscation.impl.obfuscators.value.ValueObfuscateConfig;
import com.accor.wcp.obfuscation.impl.obfuscators.xml.XmlObfuscateConfig;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Slf4j
class AuditObfuscationServiceImpl implements AuditObfuscationService {

  private final ObfuscationService obfuscationService;

  private final Map<String, List<ModuleObfuscationSettings>> settingsByAuditModuleId;

  private final Map<ModuleObfuscationSettings, ObjectObfuscateConfig>
      moduleObfuscationSettingsObjectObfuscateConfigCacheMap;

  @Setter
  private boolean obfuscationDisabled;

  public AuditObfuscationServiceImpl(
      ObfuscationService obfuscationService, AuditObfuscationSettings auditObfuscationSettings) {
    this.obfuscationService = obfuscationService;
    settingsByAuditModuleId =
        auditObfuscationSettings.getConfigs().stream()
            .collect(Collectors.groupingBy(ModuleObfuscationSettings::getId));
    obfuscationDisabled = settingsByAuditModuleId.isEmpty();
    moduleObfuscationSettingsObjectObfuscateConfigCacheMap = new HashMap<>();
  }

  @Override
  public void obfuscate(AuditReport auditReport) {
    if (obfuscationDisabled) {
      return;
    }
    auditReport.getAudits().forEach(this::obfuscateAudit);
  }

  private void obfuscateAudit(Audit audit) {
    String type = audit.getType();
    if (!settingsByAuditModuleId.containsKey(type)) {
      return;
    }
    List<ModuleObfuscationSettings> moduleObfuscationSettings = settingsByAuditModuleId.get(type);
    audit
        .getEvents()
        .forEach(auditEvent -> this.obfuscateAuditEvent(auditEvent, moduleObfuscationSettings));
  }

  private void obfuscateAuditEvent(
      AuditEvent auditEvent, List<ModuleObfuscationSettings> moduleObfuscationSettings) {
    List<ModuleObfuscationSettings> filtered =
        filterModulesSettings(auditEvent, moduleObfuscationSettings);

    filtered.forEach(
        moduleObfuscationSettings1 -> applyObfuscation(auditEvent, moduleObfuscationSettings1));
  }

  private void applyObfuscation(AuditEvent auditEvent, ModuleObfuscationSettings settings) {

    ObjectObfuscateConfig objectObfuscationConfig = getObjectObfuscateConfig(settings);
    obfuscationService.obfuscateObject(auditEvent, objectObfuscationConfig);
  }

  private ObjectObfuscateConfig getObjectObfuscateConfig(ModuleObfuscationSettings settings) {
    return moduleObfuscationSettingsObjectObfuscateConfigCacheMap.computeIfAbsent(
        settings,
        moduleObfuscationSettings -> {
          Map<String, ObfuscateConfig> obfuscationProperties =
              settings.getProperties().entrySet().stream()
                  .map(
                      e -> {
                        String propertyName = e.getKey();
                        ObfuscateConfig value = getObfuscateConfig(e);
                        return new SimpleEntry<>(propertyName, value);
                      })
                  .collect(toMap(Entry::getKey, Entry::getValue));
          return ObjectObfuscateConfigImpl.builder()
              .obfuscateConfigByProperty(obfuscationProperties)
              .build();
        });
  }

  private static ObfuscateConfig getObfuscateConfig(
      Entry<String, AuditPropertyObfuscationSettings> e) {
    AuditPropertyObfuscationSettings value = e.getValue();
    List<AuditPathConfig> payloadPaths = value.getPayloadPaths();

    // Payload (json, xml)
    if (nonNull(payloadPaths)) {
      String contentType = value.getContentType();
      if (nonNull(contentType) && contentType.contains("xml")) {
        List<XmlObfuscateConfig.PathConfig> paths =
            payloadPaths.stream()
                .map(
                    auditPathConfig ->
                        XmlObfuscateConfig.PathConfig.builder()
                            .path(auditPathConfig.getPath())
                            .maskerId(auditPathConfig.getMask())
                            .build())
                .toList();
        return XmlObfuscateConfig.builder().pathConfigs(paths).build();
      } else {
        List<JsonObfuscateConfig.PathConfig> paths =
            payloadPaths.stream()
                .map(
                    auditPathConfig ->
                        JsonObfuscateConfig.PathConfig.builder()
                            .path(auditPathConfig.getPath())
                            .maskerId(auditPathConfig.getMask())
                            .build())
                .toList();
        return JsonObfuscateConfig.builder().pathConfigs(paths).build();
      }
    }

    // Map
    List<AuditPathConfig> mapPaths = value.getMapKeys();
    if (nonNull(mapPaths)) {
      // TODO
      Map<String, ObfuscateConfig> mapProperties =
          mapPaths.stream()
              .map(
                  auditPathConfig ->
                      new SimpleEntry<>(
                          auditPathConfig.getPath(),
                          ValueObfuscateConfig.builder()
                              .maskerId(auditPathConfig.getMask())
                              .build()))
              .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
      return ObjectObfuscateConfigImpl.builder().obfuscateConfigByProperty(mapProperties).build();
    }

    // Else simple value
    return ValueObfuscateConfig.builder().maskerId(value.getValueMask()).build();
  }

  private List<ModuleObfuscationSettings> filterModulesSettings(
      AuditEvent auditEvent, List<ModuleObfuscationSettings> moduleObfuscationSettings) {
    return moduleObfuscationSettings.stream()
        .filter(settings -> this.mustWeProcessThisModuleSettings(auditEvent, settings))
        .toList();
  }

  private boolean mustWeProcessThisModuleSettings(
      AuditEvent auditEvent, ModuleObfuscationSettings settings) {
    List<ObfuscationFilter> filters = settings.getFilters();
    if (isEmpty(filters)) {
      return true;
    }

    // Find any first filter does not match
    Optional<ObfuscationFilter> filterOptional =
        filters.stream()
            .filter(obfuscationFilter -> doesObfuscationFilterMatch(auditEvent, obfuscationFilter))
            .findFirst();

    return filterOptional.isEmpty();
  }

  private boolean doesObfuscationFilterMatch(
      AuditEvent auditEvent, ObfuscationFilter obfuscationFilter) {
    String filterPath = obfuscationFilter.getPath();
    String filterExpression = obfuscationFilter.getExpression();
    try {
      Object filterValue = PropertyUtils.getNestedProperty(auditEvent, filterPath);
      if (!Pattern.matches(filterExpression, String.valueOf(filterValue))) {
        // Return true if not match
        return true;
      }
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      // Can not get value
      log.debug(
          "Can not get filter value from auditEvent: {}, filterPath: {}, error={}",
          auditEvent,
          filterPath,
          e.getMessage());
    }
    return false;
  }
}
