package com.accor.wcp.console.sdk.appmanifest;

import java.util.Collection;

/** Service to access / manage app manifests. */
public interface AppManifestService {
  Collection<AppManifest> getAppManifests();
}
