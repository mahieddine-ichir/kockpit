package com.accor.wcp.console.services.core.appmanifest.s3;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import com.accor.wcp.console.services.core.appmanifest.AppManifestRepo;
import com.accor.wcp.console.services.core.appmanifest.manifest.ApplicationManifestDtoBuilder;
import com.accor.wcp.console.services.core.appmanifest.s3.datasource.ResourceLoaderS3;
import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AppManifestS3Repo implements AppManifestRepo {

  private final ResourceLoaderS3 resourceLoaderS3;
  private final ApplicationManifestDtoBuilder applicationManifestDtoBuilder =
      new ApplicationManifestDtoBuilder();

  public AppManifestS3Repo(ResourceLoaderS3 resourceLoaderS3) {
    this.resourceLoaderS3 = resourceLoaderS3;
  }

  @Override
  public Collection<AppManifest> findAll() {
    return resourceLoaderS3.getBucketObjects().stream()
        .map(applicationManifestDtoBuilder::buildApplicationManifestsDto)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  @Override
  public Collection<? extends AppManifest> findAllWithLastModificationGreaterThan(
      Instant modificationTimestamp) {
    return resourceLoaderS3
        .getBucketObjectsWithLastModificationGreaterThan(modificationTimestamp)
        .stream()
        .map(applicationManifestDtoBuilder::buildApplicationManifestsDto)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }
}
