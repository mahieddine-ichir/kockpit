package com.accor.wcp.console.services.featureflipping.instance;

import static org.assertj.core.api.Assertions.assertThat;

import com.accor.wcp.console.sdk.topology.ManagedInstanceEvent;
import com.accor.wcp.console.services.featureflipping.instance.communication.dto.FeatureFlippingRequest;
import com.accor.wcp.sdk.service.featureflipping.communication.FeatureFlippingOperationResult;
import com.accor.wcp.sdk.service.featureflipping.communication.PropertyUpdateMessageRequestDto;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeatureFlippingInstanceManagerTest {

  public static final String DOMAIN_TEST = "domainTest";
  public static final String ENV_TEST = "envTest";
  public static final String APP_ID_TEST = "appIdTest";
  public static final String INSTANCE_ID_TEST = "instanceIdTest";

  @Mock ManagedInstanceEvent event;

  @InjectMocks
  FeatureFlippingInstanceManager underTest;

  @Test
  void should_add_one_property_to_instancePropertiesByDomainEnvApp() {
    Map<String, Object> property = new HashMap<>();
    property.put("PropertyTest", "valueTest");
    property.put("ListProperty", List.of("value1", "value2"));

    this.underTest.initInstanceProperties(
        DOMAIN_TEST, ENV_TEST, APP_ID_TEST, INSTANCE_ID_TEST, property);

    assertThat(this.underTest.getInstancesProperties(DOMAIN_TEST, ENV_TEST, APP_ID_TEST).size())
        .isEqualTo(2);
    assertThat(
            this.underTest
                .getInstancesProperties(DOMAIN_TEST, ENV_TEST, APP_ID_TEST)
                .get(0)
                .getApplicationInstance())
        .isEqualTo("instanceIdTest");
    assertThat(
            this.underTest
                .getInstancesProperties(DOMAIN_TEST, ENV_TEST, APP_ID_TEST)
                .get(0)
                .getPropertyKey())
        .isEqualTo("PropertyTest");
    assertThat(
            this.underTest
                .getInstancesProperties(DOMAIN_TEST, ENV_TEST, APP_ID_TEST)
                .get(0)
                .getCurrentValue())
        .isEqualTo("valueTest");
    assertThat(
            this.underTest
                .getInstancesProperties(DOMAIN_TEST, ENV_TEST, APP_ID_TEST)
                .get(1)
                .getPropertyKey())
        .isEqualTo("ListProperty");
    assertThat(
            this.underTest
                .getInstancesProperties(DOMAIN_TEST, ENV_TEST, APP_ID_TEST)
                .get(1)
                .getCurrentValue())
        .isEqualTo("value1, value2");
  }

  @Test
  void should_empty_map_by_domain_env_app() {
    Map<String, Object> property = new HashMap<>();
    property.put("PropertyTest", "valueTest");

    this.underTest.initInstanceProperties(
        DOMAIN_TEST, ENV_TEST, APP_ID_TEST, INSTANCE_ID_TEST, property);
    assertThat(this.underTest.getInstancesProperties(DOMAIN_TEST, ENV_TEST, APP_ID_TEST).size())
        .isEqualTo(1);

    this.underTest.resetInstancesPropertiesMap(DOMAIN_TEST, ENV_TEST, APP_ID_TEST);
    assertThat(this.underTest.getInstancesProperties(DOMAIN_TEST, ENV_TEST, APP_ID_TEST).size())
        .isZero();
  }

  @Test
  void should_empty_map_by_domain_env_app_instance() {
    Map<String, Object> property = new HashMap<>();
    property.put("PropertyTest", "valueTest");

    this.underTest.initInstanceProperties(
        DOMAIN_TEST, ENV_TEST, APP_ID_TEST, INSTANCE_ID_TEST, property);
    assertThat(this.underTest.getInstancesProperties(DOMAIN_TEST, ENV_TEST, APP_ID_TEST).size())
        .isEqualTo(1);

    this.underTest.resetInstancesPropertiesMap(
        DOMAIN_TEST, ENV_TEST, APP_ID_TEST, INSTANCE_ID_TEST);
    assertThat(this.underTest.getInstancesProperties(DOMAIN_TEST, ENV_TEST, APP_ID_TEST).size())
        .isZero();
  }

  @Test
  void should_update_property() {
    Map<String, Object> property = new HashMap<>();
    property.put("PropertyTest", "valueTest");
    List<PropertyUpdateMessageRequestDto> messages = new ArrayList<>();

    PropertyUpdateMessageRequestDto message =
        PropertyUpdateMessageRequestDto.builder()
            .propertyName("PropertyTest")
            .newValue("valueTestUpdated")
            .build();

    messages.add(message);

    this.underTest.initInstanceProperties(
        DOMAIN_TEST, ENV_TEST, APP_ID_TEST, INSTANCE_ID_TEST, property);

    FeatureFlippingRequest request =
        FeatureFlippingRequest.builder().appId(APP_ID_TEST).messages(messages).build();

    this.underTest.updateInstanceStatus(
        DOMAIN_TEST,
        ENV_TEST,
        INSTANCE_ID_TEST,
        "PropertyTest",
        request,
        FeatureFlippingOperationResult.DONE);

    assertThat(
            this.underTest
                .getInstancesProperties(DOMAIN_TEST, ENV_TEST, APP_ID_TEST)
                .get(0)
                .getCurrentValue())
        .isEqualTo("valueTestUpdated");
  }

  @Test
  void
      should_add_property_when_trying_to_uptdate_property_not_in_instancePropertiesByDomainEnvApp() {
    List<PropertyUpdateMessageRequestDto> messages = new ArrayList<>();

    PropertyUpdateMessageRequestDto message =
        PropertyUpdateMessageRequestDto.builder()
            .propertyName("PropertyTest")
            .newValue("valueTestUpdated")
            .build();

    messages.add(message);

    FeatureFlippingRequest request =
        FeatureFlippingRequest.builder().appId(APP_ID_TEST).messages(messages).build();

    this.underTest.updateInstanceStatus(
        DOMAIN_TEST,
        ENV_TEST,
        INSTANCE_ID_TEST,
        "PropertyTest",
        request,
        FeatureFlippingOperationResult.DONE);

    assertThat(this.underTest.getInstancesProperties(DOMAIN_TEST, ENV_TEST, APP_ID_TEST).size())
        .isEqualTo(1);
    assertThat(
            this.underTest
                .getInstancesProperties(DOMAIN_TEST, ENV_TEST, APP_ID_TEST)
                .get(0)
                .getPropertyKey())
        .isEqualTo("PropertyTest");
    assertThat(
            this.underTest
                .getInstancesProperties(DOMAIN_TEST, ENV_TEST, APP_ID_TEST)
                .get(0)
                .getCurrentValue())
        .isEqualTo("valueTestUpdated");
  }
}
