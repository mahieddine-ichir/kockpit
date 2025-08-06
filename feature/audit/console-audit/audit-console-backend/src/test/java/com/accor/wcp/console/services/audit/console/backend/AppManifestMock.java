package com.accor.wcp.console.services.audit.console.backend;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

@Data
@Builder
class AppManifestMock implements AppManifest {
  @Default private String domain = "it";
  private String subDomain;
  @Default private String env = "test";
  @Default private String applicationId = "applicationId";
  @Default private String applicationLabel = "applicationLabel";
  @Default private Instant lastModificationDate = Instant.now();
  @Default private String name = "AppManifestName";
  private String source;
  @Default private Collection<String> groups = new ArrayList<>();
  @Default private Collection<String> serviceIds = new ArrayList<>();
  @Default private Map<String, List<Map<String, Object>>> serviceDataMap = new HashMap<>();

  @Override
  public List<Map<String, Object>> getServiceData(String serviceId) {
    return serviceDataMap.get(serviceId);
  }
}
