package com.accor.wcp.console.services.dynaconfig.instance.communication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accor.wcp.console.sdk.service.ApplicationMessageNotification;
import com.accor.wcp.console.services.dynaconfig.instance.DynaConfigInstanceManager;
import com.accor.wcp.console.services.dynaconfig.instance.SynchronizeInstanceService;
import com.accor.wcp.console.services.dynaconfig.instance.communication.dto.DynaConfigResponse;
import com.accor.wcp.console.services.dynaconfig.instance.domain.DynaConfigInstance;
import com.accor.wcp.console.services.dynaconfig.manifest.DynaConfigPropertySettings;
import com.accor.wcp.console.services.dynaconfig.manifest.DynaConfigServiceManifest;
import com.accor.wcp.console.services.dynaconfig.manifest.DynaConfigSettingsDto;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InstanceResponseHandlerTest {

  @Mock private DynaConfigInstanceManager instanceManager;
  @Mock private SynchronizeInstanceService synchronizeInstanceService;
  @Mock private InstanceRequestHandler requestHandler;

  @InjectMocks private InstanceResponseHandler underTest;

  @Test
  void should_process_instance_update_response() {
    ApplicationMessageNotification notification = mock(ApplicationMessageNotification.class);
    when(notification.getInstanceId()).thenReturn("instanceId");
    when(notification.getDomain()).thenReturn("wcxss");
    when(notification.getEnv()).thenReturn("dev");
    when(notification.getRequestMessageId()).thenReturn("requestId");

    Map<String, Object> message = new HashMap<>();
    message.put("propertyName", "propertyKey");
    message.put("result", "DONE");
    when(notification.getMessage()).thenReturn(message);

    underTest.handleInstanceUpdateResponse(notification);

    verify(requestHandler, times(1))
        .linkResponseToRequest(eq("requestId"), any(DynaConfigResponse.class));
  }

  @Test
  void should_process_instance_multi_updates_response() {
    ApplicationMessageNotification notification = mock(ApplicationMessageNotification.class);
    when(notification.getDomain()).thenReturn("wcxss");
    when(notification.getEnv()).thenReturn("dev");
    when(notification.getInstanceId()).thenReturn("instanceId");
    when(notification.getRequestMessageId()).thenReturn("requestId");

    Map<String, Object> message = new HashMap<>();
    message.put("propertyName", "propertyKey");
    message.put("result", "DONE");

    Map<String, Object> message2 = new HashMap<>();
    message2.put("propertyName", "anotherProperty");
    message2.put("result", "DONE");
    when(notification.getMessage()).thenReturn(Map.of("results", List.of(message, message2)));

    underTest.handleInstanceMultiUpdatesResponse(notification);

    verify(requestHandler, times(2))
        .linkResponseToRequest(eq("requestId"), any(DynaConfigResponse.class));
  }

  @Test
  void should_process_instance_initialisation_message() {
    ApplicationMessageNotification notification = mock(ApplicationMessageNotification.class);
    when(notification.getDomain()).thenReturn("wcxss");
    when(notification.getEnv()).thenReturn("dev");
    when(notification.getInstanceId()).thenReturn("instanceId");
    when(notification.getApplicationId()).thenReturn("appId");

    Map<String, Set<Object>> appProperties = new HashMap<>();
    appProperties.put("propertyKey1", Set.of("true"));
    appProperties.put("propertyKey2", Set.of("string"));
    appProperties.put("propertyKeyNotInManifest", Set.of(123));
    HashSet<Object> nullHashSet = new HashSet<>();
    nullHashSet.add(null);
    appProperties.put("propertyKeyWithNull", nullHashSet);

    when(notification.getMessage()).thenReturn(Map.of("propertyValues", appProperties));

    Set<DynaConfigInstance> props = new HashSet<>();
    props.add(
        DynaConfigInstance.builder().propertyKey("propertyKey1").currentValue("true").build());
    props.add(
        DynaConfigInstance.builder().propertyKey("propertyKey2").currentValue("string").build());
    when(instanceManager.initInstanceProperties(any(), any(), any(), any(), any()))
        .thenReturn(props);

    underTest.handleInstanceInitProperties(notification, fakeManifest());

    verify(synchronizeInstanceService, times(1))
        .syncInstance("instanceId", "wcxss", "dev", "appId", props);
  }

  private DynaConfigServiceManifest fakeManifest() {
    DynaConfigPropertySettings propSetting = new DynaConfigPropertySettings();
    propSetting.setDescription("Property description");
    propSetting.setName("propertyKey1");
    propSetting.setType("boolean");

    DynaConfigPropertySettings secondPropSetting = new DynaConfigPropertySettings();
    secondPropSetting.setDescription("Property description");
    secondPropSetting.setName("propertyKey2");
    secondPropSetting.setType("string");

    DynaConfigSettingsDto settingsDto = new DynaConfigSettingsDto();
    settingsDto.setEnv("dev");
    settingsDto.setName("appId");
    settingsDto.setProperties(List.of(propSetting, secondPropSetting));

    return DynaConfigServiceManifest.builder()
        .dynaConfigSettingsMap(Map.of("wcxss-dev-appId", settingsDto))
        .build();
  }
}
