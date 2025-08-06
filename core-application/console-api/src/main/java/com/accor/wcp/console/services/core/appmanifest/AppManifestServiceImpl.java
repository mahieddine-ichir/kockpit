package com.accor.wcp.console.services.core.appmanifest;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import com.accor.wcp.console.sdk.appmanifest.AppManifestService;
import com.accor.wcp.console.services.core.appmanifest.memory.AppManifestMemoryRepo;
import com.accor.wcp.console.services.core.appmanifest.s3.AppManifestS3Repo;
import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AppManifestServiceImpl implements AppManifestService, AppManifestServiceInternal {
  private AppManifestS3Repo appManifestS3Repo;
  private AppManifestMemoryRepo appManifestMemoryRepo;

  public AppManifestServiceImpl(
      AppManifestS3Repo appManifestS3Repo, AppManifestMemoryRepo appManifestMemoryRepo) {
    this.appManifestS3Repo = appManifestS3Repo;
    this.appManifestMemoryRepo = appManifestMemoryRepo;
  }

  @Override
  public Collection<AppManifest> getAppManifests() {
    Collection<AppManifest> appManifestsS3 = appManifestS3Repo.findAll();
    Collection<AppManifest> appManifestsMemory = appManifestMemoryRepo.findAll();

    appManifestsS3.removeIf(
        appManifest -> this.isManifestOverride(appManifest, appManifestsMemory));

    return Stream.of(appManifestsS3, appManifestsMemory)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private boolean isManifestOverride(
      AppManifest s3Manifest, Collection<AppManifest> inMemoryManifests) {
    return inMemoryManifests.stream()
        .anyMatch(appManifest -> appManifest.getName().equals(s3Manifest.getName()));
  }

  @Override
  public Collection<? extends AppManifest> findUpdatedAppManifestsFromModification(
      Instant modificationTimestamp) {
    Collection<? extends AppManifest> appManifestsS3 =
        appManifestS3Repo.findAllWithLastModificationGreaterThan(modificationTimestamp);
    Collection<? extends AppManifest> appManifestsMemory =
        appManifestMemoryRepo.findAllWithLastModificationGreaterThan(modificationTimestamp);

    return Stream.of(appManifestsS3, appManifestsMemory)
        .flatMap(Collection::stream)
        .toList();
  }
}
