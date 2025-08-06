package com.accor.wcp.console.services.featureflipping.dto.mapper;

import com.accor.wcp.console.services.featureflipping.dto.FeatureFlippingDto;
import com.accor.wcp.console.services.featureflipping.dto.PropertyDto;
import com.accor.wcp.console.services.featureflipping.dto.Source;
import com.accor.wcp.console.services.featureflipping.dto.Status;
import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingDocument.PropertyDocument;
import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingDocument.PropertyHistoryDocument;
import com.accor.wcp.console.services.featureflipping.instance.communication.dto.FeatureFlippingRequest;
import com.accor.wcp.console.services.featureflipping.instance.domain.FeatureFlippingInstance;
import com.accor.wcp.console.services.featureflipping.manifest.FeatureFlippingPropertySettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FeatureFlippingDtoMapperTest {
  FeatureFlippingDtoMapper underTest;

  @BeforeEach
  void setUp() {
    FeatureFlippingPropertyDtoMapper propertyMapper = new FeatureFlippingPropertyDtoMapperImpl();
    FeatureFlippingInstancePropertyDtoMapper instanceInfosMapper = new FeatureFlippingInstancePropertyDtoMapperImpl();
    FeatureFlippingPropertyChangesDtoMapper changesMapper = new FeatureFlippingPropertyChangesDtoMapperImpl();
    FeatureFlippingIssuedCommandsDtoMapper commandsHistoryMapper = new FeatureFlippingIssuedCommandsDtoMapperImpl();

    underTest =
        new FeatureFlippingDtoMapper(
            propertyMapper, instanceInfosMapper, changesMapper, commandsHistoryMapper);
  }

  @Test
  void should_map_dto() {
    List<FeatureFlippingPropertySettings> manifestProperties = fakeManifestSettings();
    List<PropertyDocument> fakeDocuments = fakeDocuments();
    List<FeatureFlippingInstance> fakeInstancesInfo = fakeInstancesInfo();
    List<FeatureFlippingRequest> fakedRequests = fakeRequests();
    FeatureFlippingDto featureFlippingDto = underTest.mapFeatureFlippingConfigDto(
            manifestProperties, fakeDocuments, fakeInstancesInfo, fakedRequests);

    assertThat(featureFlippingDto.getProperties()).hasSize(2);
    PropertyDto propertyDto = featureFlippingDto.getProperties().get("propertyKey1");
    assertThat(propertyDto.getKey()).isEqualTo("propertyKey1");
    assertThat(propertyDto.getValue()).isEqualTo("true");
    assertThat(propertyDto.getComment()).isEqualTo("Property comment");
    assertThat(propertyDto.getDescription()).isEqualTo("Property description");
    assertThat(propertyDto.getSource()).isEqualTo(Source.MANIFEST);
    assertThat(propertyDto.getStatus()).isEqualTo(Status.NOT_SYNCHRO);
    assertThat(propertyDto.getInstances()).hasSize(2);

    PropertyDto appPropertyDto = featureFlippingDto.getProperties().get("appProperty1");
    assertThat(appPropertyDto.getKey()).isEqualTo("appProperty1");
    assertThat(appPropertyDto.getValue()).isEqualTo("123");
    assertThat(appPropertyDto.getSource()).isEqualTo(Source.APP);
  }

  private List<FeatureFlippingPropertySettings> fakeManifestSettings() {
    FeatureFlippingPropertySettings propSetting = new FeatureFlippingPropertySettings();
    propSetting.setDescription("Property description");
    propSetting.setKey("propertyKey1");
    return List.of(propSetting);
  }

  private List<PropertyDocument> fakeDocuments() {
    PropertyDocument document = new PropertyDocument();
    document.setComment("Property comment");
    document.setKey("propertyKey1");
    document.setValue("true");
    document.setHistory(
        Collections.singletonList(
            PropertyHistoryDocument.builder()
                .valueBeforeChange("false")
                .valueAfterChange("true")
                .build()));
    return List.of(document);
  }

  private List<FeatureFlippingInstance> fakeInstancesInfo() {
    FeatureFlippingInstance firstInstance = new FeatureFlippingInstance();
    firstInstance.setApplicationInstance("instance1");
    firstInstance.setCurrentValue("true");
    firstInstance.setPropertyKey("propertyKey1");

    FeatureFlippingInstance firstInstanceUnknownProp = new FeatureFlippingInstance();
    firstInstanceUnknownProp.setApplicationInstance("instance1");
    firstInstanceUnknownProp.setCurrentValue("123");
    firstInstanceUnknownProp.setPropertyKey("appProperty1");

    FeatureFlippingInstance secondInstance = new FeatureFlippingInstance();
    secondInstance.setApplicationInstance("instance2");
    secondInstance.setCurrentValue("false");
    secondInstance.setPropertyKey("propertyKey1");

    return List.of(firstInstance, firstInstanceUnknownProp, secondInstance);
  }

  private List<FeatureFlippingRequest> fakeRequests() {
    FeatureFlippingRequest request = new FeatureFlippingRequest();
    request.setRequestId("request1");
    request.setInstanceId("propertyKey1");
    request.setResponses(Collections.emptyList());
    return List.of(request);
  }
}
