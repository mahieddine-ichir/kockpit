package com.accor.wcp.console.services.sqsdlq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import com.accor.wcp.console.sdk.notification.WCPConsoleUserNotificationService;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMetadata;
import com.accor.wcp.console.services.sqsdlq.config.ApplicationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ExtendWith(MockitoExtension.class)
class SqsDlqManagerServiceActivatorTest {

  @Mock CamelContext camelContext;
  @Mock DynamoDbProcessor dynamoDbProcessor;
  @Mock ApplicationProperties properties;
  @Mock WCPConsoleUserNotificationService userNotificationService;

  @Test
  void should_load_service_audit_properties_and_build_menus_items() throws IOException {
    List<Map<String, Object>> applications =
        this.loadManifestAppProperties("/manifest/nominal_case_manifest.json");
    List<Map<String, Object>> purchaseSqsDlqProperties =
        this.getSqsDlqAppProperties(applications, "wcxss-insurance-purchase");
    List<Map<String, Object>> samplesSqsDlqProperties =
        this.getSqsDlqAppProperties(applications, "wcpsamples");

    AppManifest appManifestInsurance = Mockito.mock(AppManifest.class);
    when(appManifestInsurance.getServiceData(any())).thenAnswer(i -> purchaseSqsDlqProperties);
    AppManifest appManifestSample = Mockito.mock(AppManifest.class);
    when(appManifestSample.getServiceData(any())).thenAnswer(i -> samplesSqsDlqProperties);

    Collection<AppManifest> appManifests = new ArrayList<>();
    appManifests.add(appManifestInsurance);
    appManifests.add(appManifestSample);

    // Load
    SqsDlqManagerServiceActivator auditServiceActivator =
        new SqsDlqManagerServiceActivator(camelContext, dynamoDbProcessor, properties, userNotificationService);
    WCPConsoleServiceMetadata serviceMetadata = auditServiceActivator.load(appManifests);
    assertNotNull(serviceMetadata);
    assertEquals(4, serviceMetadata.getMenus().size());
  }

  @Test
  void should_load_service_without_duplicate_sqsdlq() throws IOException {
    List<Map<String, Object>> applications =
        this.loadManifestAppProperties("/manifest/nominal_duplicate_case_manifest.json");

    List<Map<String, Object>> duplicateRouteApplications =
        this.loadManifestAppProperties("/manifest/duplicate-sqsdlq-route-manifest.json");

    List<Map<String, Object>> duplicateDlqArnapplications =
        this.loadManifestAppProperties("/manifest/duplicate-sqsdlq-dlqarn-manifest.json");

    List<Map<String, Object>> samplesSqsDlqProperties =
        this.getSqsDlqAppProperties(applications, "wcpsamples");

    List<Map<String, Object>> samplesSqsDlqProperties2 =
        this.getSqsDlqAppProperties(duplicateRouteApplications, "wcpsamples");

    List<Map<String, Object>> samplesSqsDlqProperties3 =
        this.getSqsDlqAppProperties(duplicateDlqArnapplications, "wcpsamples");

    AppManifest appManifestSample = Mockito.mock(AppManifest.class);
    when(appManifestSample.getServiceData(any())).thenAnswer(i -> samplesSqsDlqProperties);
    AppManifest appManifestSample2 = Mockito.mock(AppManifest.class);
    when(appManifestSample2.getServiceData(any())).thenAnswer(i -> samplesSqsDlqProperties2);
    AppManifest appManifestSample3 = Mockito.mock(AppManifest.class);
    when(appManifestSample3.getServiceData(any())).thenAnswer(i -> samplesSqsDlqProperties3);

    Collection<AppManifest> appManifests = new ArrayList<>();
    appManifests.add(appManifestSample);
    appManifests.add(appManifestSample2);
    appManifests.add(appManifestSample3);

    SqsDlqManagerServiceActivator auditServiceActivator =
        new SqsDlqManagerServiceActivator(camelContext, dynamoDbProcessor, properties, userNotificationService);
    WCPConsoleServiceMetadata serviceMetadata = auditServiceActivator.load(appManifests);
    assertNotNull(serviceMetadata);
    assertEquals(1, serviceMetadata.getMenus().size());
  }

  @Test
  void should_not_load_audit_services_when_manifest_contains_invalid_values() throws IOException {
    List<Map<String, Object>> applications =
        this.loadManifestAppProperties("/manifest/faulty_properties_manifest.json");
    List<Map<String, Object>> purchaseSqsDlqProperties =
        this.getSqsDlqAppProperties(applications, "wcxss-insurance-purchase");
    List<Map<String, Object>> samplesSqsDlqProperties =
        this.getSqsDlqAppProperties(applications, "wcpsamples");

    AppManifest appManifestInsurance = Mockito.mock(AppManifest.class);
    when(appManifestInsurance.getServiceData(any())).thenAnswer(i -> purchaseSqsDlqProperties);
    AppManifest appManifestSample = Mockito.mock(AppManifest.class);
    when(appManifestSample.getServiceData(any())).thenAnswer(i -> samplesSqsDlqProperties);

    Collection<AppManifest> appManifests = new ArrayList<>();
    appManifests.add(appManifestInsurance);
    appManifests.add(appManifestSample);

    // Load
    SqsDlqManagerServiceActivator auditServiceActivator =
        new SqsDlqManagerServiceActivator(camelContext, dynamoDbProcessor, properties, userNotificationService);
    WCPConsoleServiceMetadata serviceMetadata = auditServiceActivator.load(appManifests);
    assertNotNull(serviceMetadata);
    assertEquals(3, serviceMetadata.getMenus().size());
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> loadManifestAppProperties(String fileName) throws IOException {
    InputStreamReader inputStreamReader =
        new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(fileName)));
    Map<String, Object> manifest = new ObjectMapper().readValue(inputStreamReader, Map.class);

    return (List<Map<String, Object>>) manifest.get("applications");
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> getSqsDlqAppProperties(
      List<Map<String, Object>> apps, String appName) {
    return apps.stream()
        .filter(entry -> appName.equals(entry.get("id")))
        .findFirst()
        .map(o -> o.get("services"))
        .map(o -> ((Map<String, Object>) o).get("sqsdlq"))
        .map(o -> (List<Map<String, Object>>) o)
        .orElse(Collections.emptyList());
  }
}
