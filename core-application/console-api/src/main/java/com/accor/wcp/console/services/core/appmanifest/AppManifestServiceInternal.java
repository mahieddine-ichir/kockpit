package com.accor.wcp.console.services.core.appmanifest;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;

import java.time.Instant;
import java.util.Collection;

public interface AppManifestServiceInternal {
    Collection<? extends AppManifest> findUpdatedAppManifestsFromModification(Instant lastLoadedManifestTimestamp);
}
