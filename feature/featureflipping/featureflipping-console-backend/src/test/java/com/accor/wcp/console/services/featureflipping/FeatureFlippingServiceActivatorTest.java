package com.accor.wcp.console.services.featureflipping;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMetadata;
import com.accor.wcp.console.services.featureflipping.dynamo.FeatureFlippingDocumentService;
import com.accor.wcp.console.services.featureflipping.instance.FeatureFlippingSynchronizeInstanceService;
import com.accor.wcp.console.services.featureflipping.instance.communication.FeatureFlippingInstanceResponseHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureFlippingServiceActivatorTest {

  @Mock
  FeatureFlippingInstanceResponseHandler featureFlippingInstanceResponseHandler;
  @Mock
  FeatureFlippingSynchronizeInstanceService featureFlippingSynchronizeInstanceService;
  @Mock
  FeatureFlippingDocumentService featureFlippingDocumentService;;

  @Test
  void should_load_service_ff_properties_and_build_menus_items() throws IOException {
    List<Map<String, Object>> applications =
        this.loadManifestAppProperties("/manifest/nominal_case_manifest.json");
    List<Map<String, Object>> wcpsamples =
        this.getFeatureFlippingAppProperties(applications, "wcpsamples");
    List<Map<String, Object>> wcpsamples2 =
        this.getFeatureFlippingAppProperties(applications, "another-wcpsamples");

    AppManifest appManifestSample = Mockito.mock(AppManifest.class);
    when(appManifestSample.getServiceData(any())).thenAnswer(i -> wcpsamples);
    AppManifest appManifestSample2 = Mockito.mock(AppManifest.class);
    when(appManifestSample2.getServiceData(any())).thenAnswer(i -> wcpsamples2);

    Collection<AppManifest> appManifests = new ArrayList<>();
    appManifests.add(appManifestSample);
    appManifests.add(appManifestSample2);

    // Load
    FeatureFlippingServiceActivator activator =
        new FeatureFlippingServiceActivator(featureFlippingInstanceResponseHandler, featureFlippingSynchronizeInstanceService, featureFlippingDocumentService);
    activator.init();

    WCPConsoleServiceMetadata serviceMetadata = activator.load(appManifests);
    assertNotNull(serviceMetadata);
    assertEquals(2, serviceMetadata.getMenus().size());
  }

  @Test
  void should_not_load_service_ff_properties_when_manifest_contains_invalid_values()
      throws IOException {
    List<Map<String, Object>> applications =
        this.loadManifestAppProperties("/manifest/faulty_properties_manifest.json");
    List<Map<String, Object>> wcpSamples =
        this.getFeatureFlippingAppProperties(applications, "wcpsamples");
    List<Map<String, Object>> wcpSamples2 =
        this.getFeatureFlippingAppProperties(applications, "another-wcpsamples");

    AppManifest appManifestSample = Mockito.mock(AppManifest.class);
    when(appManifestSample.getServiceData(any())).thenAnswer(i -> wcpSamples);
    AppManifest appManifestSample2 = Mockito.mock(AppManifest.class);
    when(appManifestSample2.getServiceData(any())).thenAnswer(i -> wcpSamples2);

    Collection<AppManifest> appManifests = new ArrayList<>();
    appManifests.add(appManifestSample);
    appManifests.add(appManifestSample2);

    // Load
    FeatureFlippingServiceActivator activator =
        new FeatureFlippingServiceActivator(featureFlippingInstanceResponseHandler, featureFlippingSynchronizeInstanceService, featureFlippingDocumentService);
    activator.init();

    WCPConsoleServiceMetadata serviceMetadata = activator.load(appManifests);
    assertNotNull(serviceMetadata);
    assertEquals(1, serviceMetadata.getMenus().size());
  }

  @SneakyThrows
  @Test
  void synchronize_settings_from_DB() {
    List<Map<String, Object>> applications =
        this.loadManifestAppProperties("/manifest/faulty_properties_manifest.json");
    List<Map<String, Object>> wcpSamples =
        this.getFeatureFlippingAppProperties(applications, "wcpsamples");
    List<Map<String, Object>> wcpSamples2 =
        this.getFeatureFlippingAppProperties(applications, "another-wcpsamples");

    AppManifest appManifestSample = Mockito.mock(AppManifest.class);
    when(appManifestSample.getServiceData(any())).thenAnswer(i -> wcpSamples);
    AppManifest appManifestSample2 = Mockito.mock(AppManifest.class);
    when(appManifestSample2.getServiceData(any())).thenAnswer(i -> wcpSamples2);

    Collection<AppManifest> appManifests = new ArrayList<>();
    appManifests.add(appManifestSample);
    appManifests.add(appManifestSample2);

    // Load
    FeatureFlippingServiceActivator activator =
        new FeatureFlippingServiceActivator(featureFlippingInstanceResponseHandler, featureFlippingSynchronizeInstanceService, featureFlippingDocumentService);
    activator.init();

    WCPConsoleServiceMetadata serviceMetadata = activator.load(appManifests);
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
  private List<Map<String, Object>> getFeatureFlippingAppProperties(
      List<Map<String, Object>> apps, String appName) {
    return apps.stream()
        .filter(entry -> appName.equals(entry.get("id")))
        .findFirst()
        .map(o -> o.get("services"))
        .map(o -> ((Map<String, Object>) o).get("featureflipping"))
        .map(o -> (List<Map<String, Object>>) o)
        .orElse(Collections.emptyList());
  }
}
