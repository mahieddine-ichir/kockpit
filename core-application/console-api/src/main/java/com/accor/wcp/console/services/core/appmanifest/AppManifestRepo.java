package com.accor.wcp.console.services.core.appmanifest;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;

import java.time.Instant;
import java.util.Collection;

public interface AppManifestRepo {
  Collection<AppManifest> findAll();

    Collection<? extends AppManifest> findAllWithLastModificationGreaterThan(Instant modificationTimestamp);
}
