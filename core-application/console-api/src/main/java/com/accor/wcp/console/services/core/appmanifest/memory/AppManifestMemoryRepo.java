package com.accor.wcp.console.services.core.appmanifest.memory;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import com.accor.wcp.console.services.core.appmanifest.AppManifestRepo;
import com.accor.wcp.console.services.core.appmanifest.manifest.ApplicationManifestDtoBuilder;
import com.accor.wcp.console.services.core.appmanifest.manifest.Manifest;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class AppManifestMemoryRepo implements AppManifestRepo {
  private final Map<String, List<AppManifest>> manifestInMemory;
  private final ApplicationManifestDtoBuilder applicationManifestDtoBuilder;

  public AppManifestMemoryRepo() {
    this.manifestInMemory = new HashMap<>();
    this.applicationManifestDtoBuilder = new ApplicationManifestDtoBuilder();
  }

  @Override
  public Collection<AppManifest> findAll() {
    return manifestInMemory.values().stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  public Map<String, List<AppManifest>> addManifestInMemory(Manifest manifest) {
    List<AppManifest> appManifestList = new ArrayList<>();
    appManifestList.addAll(applicationManifestDtoBuilder.buildApplicationManifestsDto(manifest));
    manifestInMemory.put(manifest.getName(), appManifestList);

    return manifestInMemory;
  }

  public void deleteManifestInMemory(String fileName) {
    manifestInMemory.remove(fileName);
  }

  @Override
  public Collection<? extends AppManifest> findAllWithLastModificationGreaterThan(
      Instant modificationTimestamp) {
    return manifestInMemory.values().stream()
        .flatMap(Collection::stream)
        .filter(timestamp -> timestamp.getLastModificationDate().isAfter(modificationTimestamp))
        .collect(Collectors.toList());
  }
}
