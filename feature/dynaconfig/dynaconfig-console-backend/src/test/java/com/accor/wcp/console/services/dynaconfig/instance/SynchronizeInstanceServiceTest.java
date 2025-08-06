package com.accor.wcp.console.services.dynaconfig.instance;

import static com.accor.wcp.sdk.service.dynaconfig.ServiceDefinition.SERVICE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accor.wcp.console.sdk.notification.NotificationLevelType;
import com.accor.wcp.console.sdk.notification.UserNotification;
import com.accor.wcp.console.sdk.notification.WCPConsoleUserNotificationService;
import com.accor.wcp.console.sdk.notification.impl.DefaultUserNotification;
import com.accor.wcp.console.sdk.topology.ApplicationInstanceManager;
import com.accor.wcp.console.sdk.topology.ManagedInstanceEvent;
import com.accor.wcp.console.services.dynaconfig.dynamo.DynaConfigDocumentService;
import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigDocument;
import com.accor.wcp.console.services.dynaconfig.instance.communication.InstanceRequestHandler;
import com.accor.wcp.console.services.dynaconfig.instance.domain.DynaConfigInstance;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SynchronizeInstanceServiceTest {

  @Mock private DynaConfigDocumentService documentService;
  @Mock private DynaConfigInstanceManager instanceManager;
  @Mock private InstanceRequestHandler instanceRequestHandler;
  @Mock ApplicationInstanceManager applicationInstanceManager;
  @Mock WCPConsoleUserNotificationService consoleUserNotificationService;

  @InjectMocks private SynchronizeInstanceService underTest;

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
//    verify(instanceRequestHandler, times(1))
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
    Set<DynaConfigInstance> appProperties = new HashSet<>();
    appProperties.add(
        DynaConfigInstance.builder().propertyKey("propertyKey1").currentValue("true").build());
    appProperties.add(
        DynaConfigInstance.builder().propertyKey("propertyKey2").currentValue("string").build());

    when(documentService.getDynaConfigDocument("wcxss", "dev", "appId"))
        .thenReturn(Optional.of(fakeDocument()));

    underTest.syncInstance("instanceId", "wcxss", "dev", "appId", appProperties);

    verify(instanceRequestHandler, times(1))
        .sendMultiUpdatesRequestToInstance(
            "wcxss", "dev", "appId", "instanceId",
                Map.of("propertyKey1", "false", "propertyKey2", "string"));
    verify(documentService, times(0)).updateDocumentProperty(any(), any(), any(), any(), any());
  }

  @Test
  void when_all_instance_props_sync_should_send_update_request_to_instance() {
    Set<DynaConfigInstance> appProperties = new HashSet<>();
    appProperties.add(
        DynaConfigInstance.builder().propertyKey("propertyKey1").currentValue("false").build());
    appProperties.add(
        DynaConfigInstance.builder().propertyKey("propertyKey2").currentValue("string").build());

    when(documentService.getDynaConfigDocument("wcxss", "dev", "appId"))
        .thenReturn(Optional.of(fakeDocument()));

    underTest.syncInstance("instanceId", "wcxss", "dev", "appId", appProperties);

    verify(instanceRequestHandler, times(1))
        .sendMultiUpdatesRequestToInstance(
            "wcxss", "dev", "appId", "instanceId",
                Map.of("propertyKey1", "false", "propertyKey2", "string"));
    verify(documentService, times(0)).updateDocumentProperty(any(), any(), any(), any(), any());
  }

  @Test
  void when_instance_props_unknown_should_save_property_value_in_repository() {
    Set<DynaConfigInstance> appProperties = new HashSet<>();
    appProperties.add(
        DynaConfigInstance.builder().propertyKey("newProperty").currentValue("string").build());

    when(documentService.getDynaConfigDocument("wcxss", "dev", "appId"))
        .thenReturn(Optional.of(fakeDocument()));

    underTest.syncInstance("instanceId", "wcxss", "dev", "appId", appProperties);

    verify(instanceRequestHandler, times(1))
        .sendMultiUpdatesRequestToInstance(
            "wcxss", "dev", "appId", "instanceId", Collections.emptyMap());
    verify(documentService, times(1))
        .updateDocumentProperty("wcxss", "dev", "appId", "newProperty", "string");
  }

  // TODO - review feature
//  @Test
  void
      when_saved_props_are_not_present_in_instance_props_should_notify_orphans_property() {
    Set<DynaConfigInstance> appProperties = new HashSet<>();
    String propertyKey1 = "propertyKey1";
    String propertyKey2 = "propertyKey2";
    appProperties.add(
        DynaConfigInstance.builder().propertyKey(propertyKey2).currentValue("string").build());

    String appId = "appId";
    String domain = "wcxss";
    String env = "dev";
    when(documentService.getDynaConfigDocument(domain, env, appId))
        .thenReturn(Optional.of(fakeDocument()));

    String instanceId = "instanceId";
    underTest.syncInstance(instanceId, domain, env, appId, appProperties);

    ArgumentCaptor<UserNotification> userNotificationArgumentCaptor = ArgumentCaptor.captor();
    verify(consoleUserNotificationService, times(1))
        .create(eq(SERVICE_ID), userNotificationArgumentCaptor.capture());

    String description = String.format("Orphan property: %s found from instanceId: %s", propertyKey1, instanceId);
    UserNotification userNotification = userNotificationArgumentCaptor.getValue();
    assertEquals(SERVICE_ID, userNotification.getServiceId());
    assertEquals(appId, userNotification.getApplicationId());
    assertEquals(NotificationLevelType.INFO, userNotification.getLevel());
    assertEquals(description, userNotification.getDescription());
  }

  private DynaConfigDocument fakeDocument() {
    DynaConfigDocument.PropertyDocument prop = new DynaConfigDocument.PropertyDocument();
    prop.setName("propertyKey1");
    prop.setValue("false");

    DynaConfigDocument.PropertyDocument prop2 = new DynaConfigDocument.PropertyDocument();
    prop2.setName("propertyKey2");
    prop2.setValue("string");

    Map<String, DynaConfigDocument.PropertyDocument> properties = new HashMap<>();
    properties.put("propertyKey1", prop);
    properties.put("propertyKey2", prop2);

    return DynaConfigDocument.builder().propertyValues(properties).build();
  }
}
