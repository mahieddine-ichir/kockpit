package com.accor.wcp.console.sdk.appmanifest;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Application manifest model properties. */
public interface AppManifest {
  String getDomain();

  String getSubDomain();

  String getEnv();

  String getApplicationId();

  String getApplicationLabel();

  Instant getLastModificationDate();

  String getName();

  String getSource();

  Collection<String> getGroups();

  Collection<String> getServiceIds();

  List<Map<String, Object>> getServiceData(String serviceId);
}
