package com.accor.wcp.console.services.dynaconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMetadata;
import com.accor.wcp.console.services.dynaconfig.instance.SynchronizeInstanceService;
import com.accor.wcp.console.services.dynaconfig.instance.communication.InstanceResponseHandler;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DynaConfigServiceActivatorTest {

  @Mock InstanceResponseHandler instanceResponseHandler;
  @Mock SynchronizeInstanceService synchronizeInstanceService;

  @Test
  void should_load_service_dynaconfig_properties_and_build_menus_items() throws IOException {
    List<Map<String, Object>> applications =
        this.loadManifestAppProperties("/manifest/nominal_case_manifest.json");
    List<Map<String, Object>> wcpsamples =
        this.getDynaConfigAppProperties(applications, "wcpsamples");
    List<Map<String, Object>> wcpsamples2 =
        this.getDynaConfigAppProperties(applications, "another-wcpsamples");

    AppManifest appManifestSample = Mockito.mock(AppManifest.class);
    when(appManifestSample.getServiceData(any())).thenAnswer(i -> wcpsamples);
    AppManifest appManifestSample2 = Mockito.mock(AppManifest.class);
    when(appManifestSample2.getServiceData(any())).thenAnswer(i -> wcpsamples2);

    Collection<AppManifest> appManifests = new ArrayList<>();
    appManifests.add(appManifestSample);
    appManifests.add(appManifestSample2);

    // Load
    DynaConfigServiceActivator dynaConfigActivator =
        new DynaConfigServiceActivator(instanceResponseHandler, synchronizeInstanceService);
    WCPConsoleServiceMetadata serviceMetadata = dynaConfigActivator.load(appManifests);
    assertNotNull(serviceMetadata);
    assertEquals(2, serviceMetadata.getMenus().size());
  }

  @Test
  void should_not_load_service_dynaconfig_properties_when_manifest_contains_invalid_values()
      throws IOException {
    List<Map<String, Object>> applications =
        this.loadManifestAppProperties("/manifest/faulty_properties_manifest.json");
    List<Map<String, Object>> wcpSamples =
        this.getDynaConfigAppProperties(applications, "wcpsamples");
    List<Map<String, Object>> wcpSamples2 =
        this.getDynaConfigAppProperties(applications, "another-wcpsamples");

    AppManifest appManifestSample = Mockito.mock(AppManifest.class);
    when(appManifestSample.getServiceData(any())).thenAnswer(i -> wcpSamples);
    AppManifest appManifestSample2 = Mockito.mock(AppManifest.class);
    when(appManifestSample2.getServiceData(any())).thenAnswer(i -> wcpSamples2);

    Collection<AppManifest> appManifests = new ArrayList<>();
    appManifests.add(appManifestSample);
    appManifests.add(appManifestSample2);

    // Load
    DynaConfigServiceActivator dynaConfigActivator =
        new DynaConfigServiceActivator(instanceResponseHandler, synchronizeInstanceService);
    WCPConsoleServiceMetadata serviceMetadata = dynaConfigActivator.load(appManifests);
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
  private List<Map<String, Object>> getDynaConfigAppProperties(
      List<Map<String, Object>> apps, String appName) {
    return apps.stream()
        .filter(entry -> appName.equals(entry.get("id")))
        .findFirst()
        .map(o -> o.get("services"))
        .map(o -> ((Map<String, Object>) o).get("dynaconfig"))
        .map(o -> (List<Map<String, Object>>) o)
        .orElse(Collections.emptyList());
  }
}
