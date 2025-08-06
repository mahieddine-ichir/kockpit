package com.accor.wcp.console.services.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMetadata;
import com.accor.wcp.console.sdk.topology.ApplicationInstanceManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CacheManagerServiceActivatorTest {

  @Test
  void should_load_service_cache_properties_and_build_menus_items() throws IOException {
    List<Map<String, Object>> applications =
        this.loadManifestAppProperties("/manifest/nominal_case_manifest.json");
    List<Map<String, Object>> insuranceV1Cache =
        this.getAuditAppProperties(applications, "wcxss-insurance-quotation-v1");
    List<Map<String, Object>> insuranceV2Cache =
        this.getAuditAppProperties(applications, "wcxss-insurance-quotation-v2");

    AppManifest appManifestInsurance = Mockito.mock(AppManifest.class);
    when(appManifestInsurance.getServiceData(any())).thenAnswer(i -> insuranceV1Cache);
    AppManifest appManifestRestaurantApi = Mockito.mock(AppManifest.class);
    when(appManifestRestaurantApi.getServiceData(any())).thenAnswer(i -> insuranceV2Cache);

    Collection<AppManifest> appManifests = new ArrayList<>();
    appManifests.add(appManifestInsurance);
    appManifests.add(appManifestRestaurantApi);

    ApplicationInstanceManager applicationInstanceManager = null;

    // Load
    CacheManagerServiceActivator cacheManagerActivator =
        new CacheManagerServiceActivator(
            new StateManager(applicationInstanceManager), new HistoCommandManager());
    WCPConsoleServiceMetadata serviceMetadata = cacheManagerActivator.load(appManifests);
    assertNotNull(serviceMetadata);
    assertEquals(2, serviceMetadata.getMenus().size());
  }

  @Test
  void should_not_load_service_cache_properties_when_manifest_contains_invalid_values()
      throws IOException {
    List<Map<String, Object>> applications =
        this.loadManifestAppProperties("/manifest/faulty_properties_manifest.json");
    List<Map<String, Object>> insuranceV1Cache =
        this.getAuditAppProperties(applications, "wcxss-insurance-quotation-v1");
    List<Map<String, Object>> insuranceV2Cache =
        this.getAuditAppProperties(applications, "wcxss-insurance-quotation-v2");

    AppManifest appManifestInsurance = Mockito.mock(AppManifest.class);
    when(appManifestInsurance.getServiceData(any())).thenAnswer(i -> insuranceV1Cache);
    AppManifest appManifestRestaurantApi = Mockito.mock(AppManifest.class);
    when(appManifestRestaurantApi.getServiceData(any())).thenAnswer(i -> insuranceV2Cache);

    Collection<AppManifest> appManifests = new ArrayList<>();
    appManifests.add(appManifestInsurance);
    appManifests.add(appManifestRestaurantApi);

    ApplicationInstanceManager applicationInstanceManager = null;

    // Load
    CacheManagerServiceActivator cacheManagerActivator =
        new CacheManagerServiceActivator(
            new StateManager(applicationInstanceManager), new HistoCommandManager());
    WCPConsoleServiceMetadata serviceMetadata = cacheManagerActivator.load(appManifests);
    assertNotNull(serviceMetadata);
    assertEquals(1, serviceMetadata.getMenus().size());
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> loadManifestAppProperties(String fileName) throws IOException {
    InputStreamReader inputStreamReader =
        new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(fileName)));
    Map<String, Object> manifest = new ObjectMapper().readValue(inputStreamReader, Map.class);

    return (List<Map<String, Object>>) manifest.get("applications");
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> getAuditAppProperties(
      List<Map<String, Object>> apps, String appName) {
    return apps.stream()
        .filter(entry -> appName.equals(entry.get("id")))
        .findFirst()
        .map(o -> o.get("services"))
        .map(o -> ((Map<String, Object>) o).get("cache"))
        .map(o -> (List<Map<String, Object>>) o)
        .orElse(Collections.emptyList());
  }
}
