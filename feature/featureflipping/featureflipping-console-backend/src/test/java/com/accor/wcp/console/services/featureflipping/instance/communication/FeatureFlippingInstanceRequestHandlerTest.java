package com.accor.wcp.console.services.featureflipping.instance.communication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accor.wcp.console.sdk.communication.WCPConsole2AppCommunicationService;
import com.accor.wcp.console.services.featureflipping.instance.communication.dto.FeatureFlippingRequest;
import com.accor.wcp.console.services.featureflipping.instance.communication.dto.FeatureFlippingResponse;
import com.accor.wcp.sdk.service.featureflipping.communication.InstanceInitPropertiesUpdateRequestDto;
import com.accor.wcp.sdk.service.featureflipping.communication.PropertyUpdateMessageRequestDto;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeatureFlippingInstanceRequestHandlerTest {

  FeatureFlippingInstanceRequestHandler underTest;

  @Mock WCPConsole2AppCommunicationService communicationService;

  @BeforeEach
  void setup() {

    this.underTest = new FeatureFlippingInstanceRequestHandler(this.communicationService);
  }

  @Test
  void should_return_id_and_add_to_requests() {
    when(this.communicationService.broadcast(
            eq("featureflipping"), eq("domainTest"), eq("envTest"), eq("applicationIdTest"), any()))
        .thenReturn("idtest");

    String id =
        this.underTest.broadcastUpdateRequest(
            "domainTest", "envTest", "applicationIdTest", "propKey", "propValue");
    assertThat(id).isEqualTo("idtest");

    verify(communicationService, times(1))
        .broadcast(
            eq("featureflipping"), eq("domainTest"), eq("envTest"), eq("applicationIdTest"), any());

    FeatureFlippingRequest featureFlippingRequest =
        this.underTest.getRequests("domainTest", "envTest", "applicationIdTest").get(0);
    assertThat(this.underTest.getRequests("domainTest", "envTest", "applicationIdTest").size())
        .isEqualTo(1);
    assertThat(featureFlippingRequest.getRequestId()).isEqualTo("idtest");
    assertThat(featureFlippingRequest.getDomain()).isEqualTo("domainTest");
    assertThat(featureFlippingRequest.getEnv()).isEqualTo("envTest");
    assertThat(featureFlippingRequest.getAppId()).isEqualTo("applicationIdTest");
    assertThat(featureFlippingRequest.getInstanceId()).isNull();

    assertThat(featureFlippingRequest.getMessages().size()).isEqualTo(1);
    assertThat(featureFlippingRequest.getMessages().get(0).getPropertyName()).isEqualTo("propKey");
    assertThat(featureFlippingRequest.getMessages().get(0).getNewValue()).isEqualTo("propValue");

    ArgumentCaptor<PropertyUpdateMessageRequestDto> argument =
        ArgumentCaptor.forClass(PropertyUpdateMessageRequestDto.class);
    verify(communicationService)
        .broadcast(
            eq("featureflipping"),
            eq("domainTest"),
            eq("envTest"),
            eq("applicationIdTest"),
            argument.capture());

    PropertyUpdateMessageRequestDto property = argument.getValue();
    assertThat(property.getPropertyName()).isEqualTo("propKey");
    assertThat(property.getNewValue()).isEqualTo("propValue");
  }

  @Test
  void shouldReturnIdTestWhenBroadCastMultiUpdatesRequest() {
    when(this.communicationService.broadcast(
            eq("featureflipping"), eq("domainTest"), eq("envTest"), eq("applicationIdTest"), any()))
        .thenReturn("idtest");

    Map<String, String> properties = new HashMap<>();
    properties.put("prop1", "value1");
    properties.put("prop2", "value2");

    String id =
        this.underTest.broadcastMultiUpdatesRequest(
            "domainTest", "envTest", "applicationIdTest", properties);
    assertThat(id).isEqualTo("idtest");

    verify(communicationService, times(1))
        .broadcast(
            eq("featureflipping"), eq("domainTest"), eq("envTest"), eq("applicationIdTest"), any());

    FeatureFlippingRequest featureFlippingRequest =
        this.underTest.getRequests("domainTest", "envTest", "applicationIdTest").get(0);

    assertThat(this.underTest.getRequests("domainTest", "envTest", "applicationIdTest").size())
        .isEqualTo(1);

    assertThat(featureFlippingRequest.getRequestId()).isEqualTo("idtest");
    assertThat(featureFlippingRequest.getDomain()).isEqualTo("domainTest");
    assertThat(featureFlippingRequest.getEnv()).isEqualTo("envTest");
    assertThat(featureFlippingRequest.getAppId()).isEqualTo("applicationIdTest");
    assertThat(featureFlippingRequest.getInstanceId()).isNull();

    assertThat(featureFlippingRequest.getMessages().size()).isEqualTo(2);

    assertThat(featureFlippingRequest.getMessages().get(0).getPropertyName()).isEqualTo("prop2");
    assertThat(featureFlippingRequest.getMessages().get(0).getNewValue()).isEqualTo("value2");

    assertThat(featureFlippingRequest.getMessages().get(1).getPropertyName()).isEqualTo("prop1");
    assertThat(featureFlippingRequest.getMessages().get(1).getNewValue()).isEqualTo("value1");

    ArgumentCaptor<InstanceInitPropertiesUpdateRequestDto> argument =
        ArgumentCaptor.forClass(InstanceInitPropertiesUpdateRequestDto.class);

    verify(communicationService)
        .broadcast(
            eq("featureflipping"),
            eq("domainTest"),
            eq("envTest"),
            eq("applicationIdTest"),
            argument.capture());
    InstanceInitPropertiesUpdateRequestDto property = argument.getValue();

    assertThat(property.getUpdates().size()).isEqualTo(2);
    assertThat(property.getUpdates().get(0).getPropertyName()).isEqualTo("prop2");
    assertThat(property.getUpdates().get(0).getNewValue()).isEqualTo("value2");
    assertThat(property.getUpdates().get(1).getPropertyName()).isEqualTo("prop1");
    assertThat(property.getUpdates().get(1).getNewValue()).isEqualTo("value1");
  }

  @Test
  void shouldReturnIdTestWhenSendingMultipleMessage() {
    when(this.communicationService.send(
            eq("featureflipping"),
            eq("domainTest"),
            eq("envTest"),
            eq("applicationIdTest"),
            eq("instanceIdTest"),
            any()))
        .thenReturn("idtestsend");

    Map<String, String> properties = new HashMap<>();
    properties.put("prop1", "value1");
    properties.put("prop2", "value2");

    String id =
        this.underTest.sendMultiUpdatesRequestToInstance(
            "domainTest", "envTest", "applicationIdTest", "instanceIdTest", properties);
    assertThat(id).isEqualTo("idtestsend");
    verify(communicationService, times(1))
        .send(
            eq("featureflipping"),
            eq("domainTest"),
            eq("envTest"),
            eq("applicationIdTest"),
            eq("instanceIdTest"),
            any());

    ArgumentCaptor<InstanceInitPropertiesUpdateRequestDto> argument =
        ArgumentCaptor.forClass(InstanceInitPropertiesUpdateRequestDto.class);

    verify(communicationService)
        .send(
            eq("featureflipping"),
            eq("domainTest"),
            eq("envTest"),
            eq("applicationIdTest"),
            eq("instanceIdTest"),
            argument.capture());
    InstanceInitPropertiesUpdateRequestDto property = argument.getValue();

    assertThat(property.getUpdates().size()).isEqualTo(2);
    assertThat(property.getUpdates().get(0).getPropertyName()).isEqualTo("prop2");
    assertThat(property.getUpdates().get(0).getNewValue()).isEqualTo("value2");
    assertThat(property.getUpdates().get(1).getPropertyName()).isEqualTo("prop1");
    assertThat(property.getUpdates().get(1).getNewValue()).isEqualTo("value1");

    assertThat(this.underTest.getRequests("domainTest", "envTest", "applicationIdTest").size())
        .isEqualTo(1);
  }

  @Test
  void shouldReturnIdTestWhenBroadcastRefreshRequest() {
    when(this.communicationService.broadcast(
            eq("featureflipping"), eq("domainTest"), eq("envTest"), eq("applicationIdTest"), any()))
        .thenReturn("idtestsend");

    String id =
        this.underTest.broadcastRefreshRequest("domainTest", "envTest", "applicationIdTest");
    assertThat(id).isEqualTo("idtestsend");
    verify(communicationService, times(1))
        .broadcast(
            eq("featureflipping"), eq("domainTest"), eq("envTest"), eq("applicationIdTest"), any());

    assertThat(this.underTest.getRequests("domainTest", "envTest", "applicationIdTest").size())
        .isZero();
  }

  @Test
  void should_send_refresh_request_to_instance_with_propers_parameters() {
    when(this.communicationService.send(
            eq("featureflipping"),
            eq("domainTest"),
            eq("envTest"),
            eq("applicationIdTest"),
            eq("instanceId"),
            any()))
        .thenReturn("idtestsend");

    String id =
        this.underTest.sendRefreshRequestToInstance(
            "domainTest", "envTest", "applicationIdTest", "instanceId");
    assertThat(id).isEqualTo("idtestsend");
    verify(communicationService, times(1))
        .send(
            eq("featureflipping"),
            eq("domainTest"),
            eq("envTest"),
            eq("applicationIdTest"),
            eq("instanceId"),
            any());

    assertThat(this.underTest.getRequests("domainTest", "envTest", "applicationIdTest").size())
        .isZero();
  }

  @Test
  void should_add_to_request_response() {
    when(this.communicationService.broadcast(
            eq("featureflipping"), eq("domainTest"), eq("envTest"), eq("applicationIdTest"), any()))
        .thenReturn("idtest");

    this.underTest.broadcastUpdateRequest(
        "domainTest", "envTest", "applicationIdTest", "propKey", "propValue");

    FeatureFlippingResponse response =
        FeatureFlippingResponse.builder()
            .instanceId("instanceId")
            .message(null)
            .requestId("idtest")
            .timestamp(new Date().getTime())
            .build();

    assertThat(this.underTest.linkResponseToRequest("idtest", response))
        .isEqualTo(this.underTest.getRequestById("idtest"));
    assertThat(this.underTest.getRequestById("idtest").getResponses().size()).isEqualTo(1);
    assertThat(this.underTest.getRequestById("idtest").getResponses().get(0)).isEqualTo(response);
  }

  @Test
  void should_not_add_to_request_response() {
    when(this.communicationService.broadcast(
            eq("featureflipping"), eq("domainTest"), eq("envTest"), eq("applicationIdTest"), any()))
        .thenReturn("idtest");

    this.underTest.broadcastUpdateRequest(
        "domainTest", "envTest", "applicationIdTest", "propKey", "propValue");

    FeatureFlippingResponse response =
        FeatureFlippingResponse.builder()
            .instanceId("instanceId")
            .message(null)
            .requestId("wrongidtest")
            .timestamp(new Date().getTime())
            .build();

    assertThat(this.underTest.linkResponseToRequest("wrongidtest", response)).isNull();
  }
}
