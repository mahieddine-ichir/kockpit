package com.accor.wcp.console.services.core.appmanifest.manifest;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApplicationManifestDto implements AppManifest {
  private String domain;
  private String subDomain;
  private String env;
  private String id;
  private String label;
  private List<String> groups;
  private Map<String, List<Map<String, Object>>> services;
  private String name;
  private Instant lastModificationDate;
  private String source;

  @Override
  public String getApplicationId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getApplicationLabel() {
    return label;
  }

  @Override
  public Instant getLastModificationDate() {
    return lastModificationDate;
  }

  @Override
  public String getSource() {
    return source;
  }

  @Override
  public Collection<String> getServiceIds() {
    return services.keySet();
  }

  @Override
  public List<Map<String, Object>> getServiceData(String serviceId) {
    return Optional.ofNullable(services.get(serviceId)).orElse(Collections.emptyList());
  }
}
