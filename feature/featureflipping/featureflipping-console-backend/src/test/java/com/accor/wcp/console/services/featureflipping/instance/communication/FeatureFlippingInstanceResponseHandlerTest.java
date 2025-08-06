package com.accor.wcp.console.services.featureflipping.instance.communication;

import com.accor.wcp.console.sdk.service.ApplicationMessageNotification;
import com.accor.wcp.console.services.featureflipping.instance.FeatureFlippingInstanceManager;
import com.accor.wcp.console.services.featureflipping.instance.FeatureFlippingSynchronizeInstanceService;
import com.accor.wcp.console.services.featureflipping.instance.communication.dto.FeatureFlippingResponse;
import com.accor.wcp.console.services.featureflipping.instance.domain.FeatureFlippingInstance;
import com.accor.wcp.console.services.featureflipping.manifest.FeatureFlippingPropertySettings;
import com.accor.wcp.console.services.featureflipping.manifest.FeatureFlippingServiceManifest;
import com.accor.wcp.console.services.featureflipping.manifest.FeatureFlippingSettingsDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureFlippingInstanceResponseHandlerTest {

  @Mock private FeatureFlippingInstanceManager instanceManager;
  @Mock private FeatureFlippingSynchronizeInstanceService featureFlippingSynchronizeInstanceService;
  @Mock private FeatureFlippingInstanceRequestHandler requestHandler;

  @InjectMocks private FeatureFlippingInstanceResponseHandler underTest;

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
        .linkResponseToRequest(eq("requestId"), any(FeatureFlippingResponse.class));
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
        .linkResponseToRequest(eq("requestId"), any(FeatureFlippingResponse.class));
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

    Set<FeatureFlippingInstance> props = new HashSet<>();
    props.add(
        FeatureFlippingInstance.builder().propertyKey("propertyKey1").currentValue("true").build());
    props.add(
        FeatureFlippingInstance.builder().propertyKey("propertyKey2").currentValue("string").build());
    when(instanceManager.initInstanceProperties(any(), any(), any(), any(), any()))
        .thenReturn(props);

    underTest.handleInstanceInitProperties(notification, fakeManifest());

    verify(featureFlippingSynchronizeInstanceService, times(1))
        .syncInstance("instanceId", "wcxss", "dev", "appId", props);
  }

  private FeatureFlippingServiceManifest fakeManifest() {
    FeatureFlippingPropertySettings propSetting = new FeatureFlippingPropertySettings();
    propSetting.setDescription("Property description");
    propSetting.setKey("propertyKey1");

    FeatureFlippingPropertySettings secondPropSetting = new FeatureFlippingPropertySettings();
    secondPropSetting.setDescription("Property description");
    secondPropSetting.setKey("propertyKey2");

    FeatureFlippingSettingsDto settingsDto = new FeatureFlippingSettingsDto();
    settingsDto.setEnv("dev");
    settingsDto.setName("appId");
    settingsDto.setKeys(List.of(propSetting, secondPropSetting));

    return FeatureFlippingServiceManifest.builder()
        .featureFlippingSettingsMap(Map.of("wcxss-dev-appId", settingsDto))
        .build();
  }
}
