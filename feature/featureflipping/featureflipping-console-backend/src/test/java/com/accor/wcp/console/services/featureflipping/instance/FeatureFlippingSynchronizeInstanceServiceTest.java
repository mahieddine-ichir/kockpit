package com.accor.wcp.console.services.featureflipping.instance;

import static com.accor.wcp.sdk.service.featureflipping.ServiceDefinition.SERVICE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accor.wcp.console.sdk.notification.NotificationLevelType;
import com.accor.wcp.console.sdk.notification.UserNotification;
import com.accor.wcp.console.sdk.notification.WCPConsoleUserNotificationService;
import com.accor.wcp.console.sdk.topology.ApplicationInstanceManager;
import com.accor.wcp.console.sdk.topology.ManagedInstanceEvent;
import com.accor.wcp.console.services.featureflipping.dynamo.FeatureFlippingDocumentService;
import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingDocument;
import com.accor.wcp.console.services.featureflipping.instance.communication.FeatureFlippingInstanceRequestHandler;
import com.accor.wcp.console.services.featureflipping.instance.domain.FeatureFlippingInstance;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeatureFlippingSynchronizeInstanceServiceTest {

  @Mock private FeatureFlippingDocumentService documentService;
  @Mock private FeatureFlippingInstanceManager instanceManager;
  @Mock private FeatureFlippingInstanceRequestHandler featureFlippingInstanceRequestHandler;
  @Mock ApplicationInstanceManager applicationInstanceManager;
  @Mock WCPConsoleUserNotificationService consoleUserNotificationService;

  @InjectMocks private FeatureFlippingSynchronizeInstanceService underTest;

//  @Test
//  void when_new_instance_event_should_send_refresh_event() {
//    ManagedInstanceEvent event =
//        ManagedInstanceEvent.builder()
//            .domain("wcxss")
//            .env("dev")
//            .applicationId("appId")
//            .applicationInstance("instanceId")
//            .build();
//    underTest.onNewInstance(event);
//
//    verify(featureFlippingInstanceRequestHandler, times(1))
//        .sendRefreshRequestToInstance("wcxss", "dev", "appId", "instanceId");
//  }

  @Test
  void when_instance_removed_should_clean_instance_cache() {
    ManagedInstanceEvent event =
        ManagedInstanceEvent.builder()
            .domain("wcxss")
            .env("dev")
            .applicationId("appId")
            .applicationInstance("instanceId")
            .build();
    underTest.onRemoveInstance(event);

    verify(instanceManager, times(1))
        .resetInstancesPropertiesMap("wcxss", "dev", "appId", "instanceId");
  }

  @Test
  void when_instance_props_not_sync_should_send_update_request_to_instance() {
    Set<FeatureFlippingInstance> appProperties = new HashSet<>();
    appProperties.add(
        FeatureFlippingInstance.builder().propertyKey("propertyKey1").currentValue("true").build());
    appProperties.add(
        FeatureFlippingInstance.builder().propertyKey("propertyKey2").currentValue("string").build());

    when(documentService.getFeatureFlippingDocument("wcxss", "dev", "appId"))
        .thenReturn(Optional.of(fakeDocument()));

    underTest.syncInstance("instanceId", "wcxss", "dev", "appId", appProperties);

    verify(featureFlippingInstanceRequestHandler, times(1))
        .sendMultiUpdatesRequestToInstance(
            "wcxss", "dev", "appId", "instanceId",
                Map.of("propertyKey1", "false", "propertyKey2", "string"));
    verify(documentService, times(0)).updateDocumentProperty(any(), any(), any(), any(), any());
  }

  @Test
  void when_all_instance_props_sync_should_send_update_request_to_instance() {
    Set<FeatureFlippingInstance> appProperties = new HashSet<>();
    appProperties.add(
        FeatureFlippingInstance.builder().propertyKey("propertyKey1").currentValue("false").build());
    appProperties.add(
        FeatureFlippingInstance.builder().propertyKey("propertyKey2").currentValue("string").build());

    when(documentService.getFeatureFlippingDocument("wcxss", "dev", "appId"))
        .thenReturn(Optional.of(fakeDocument()));

    underTest.syncInstance("instanceId", "wcxss", "dev", "appId", appProperties);

    verify(featureFlippingInstanceRequestHandler, times(1))
        .sendMultiUpdatesRequestToInstance(
            "wcxss", "dev", "appId", "instanceId",
                Map.of("propertyKey1", "false", "propertyKey2", "string"));
    verify(documentService, times(0)).updateDocumentProperty(any(), any(), any(), any(), any());
  }

  @Test
  void when_instance_props_unknown_should_save_property_value_in_repository() {
    Set<FeatureFlippingInstance> appProperties = new HashSet<>();
    appProperties.add(
        FeatureFlippingInstance.builder().propertyKey("newProperty").currentValue("string").build());

    when(documentService.getFeatureFlippingDocument("wcxss", "dev", "appId"))
        .thenReturn(Optional.of(fakeDocument()));

    underTest.syncInstance("instanceId", "wcxss", "dev", "appId", appProperties);

    verify(featureFlippingInstanceRequestHandler, times(1))
        .sendMultiUpdatesRequestToInstance(
            "wcxss", "dev", "appId", "instanceId", Map.of("propertyKey1", "false", "propertyKey2", "string"));
  }

  private FeatureFlippingDocument fakeDocument() {
    FeatureFlippingDocument.PropertyDocument prop = new FeatureFlippingDocument.PropertyDocument();
    prop.setKey("propertyKey1");
    prop.setValue("false");

    FeatureFlippingDocument.PropertyDocument prop2 = new FeatureFlippingDocument.PropertyDocument();
    prop2.setKey("propertyKey2");
    prop2.setValue("string");

    Map<String, FeatureFlippingDocument.PropertyDocument> properties = new HashMap<>();
    properties.put("propertyKey1", prop);
    properties.put("propertyKey2", prop2);

    return FeatureFlippingDocument.builder().propertyValues(properties).build();
  }
}
