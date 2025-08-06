package com.accor.wcp.console.services.dynaconfig.dto.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.accor.wcp.console.services.dynaconfig.dto.DynaConfigDto;
import com.accor.wcp.console.services.dynaconfig.dto.PropertyDto;
import com.accor.wcp.console.services.dynaconfig.dto.Source;
import com.accor.wcp.console.services.dynaconfig.dto.Status;
import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigDocument.PropertyDocument;
import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigDocument.PropertyHistoryDocument;
import com.accor.wcp.console.services.dynaconfig.instance.communication.dto.DynaConfigRequest;
import com.accor.wcp.console.services.dynaconfig.instance.domain.DynaConfigInstance;
import com.accor.wcp.console.services.dynaconfig.manifest.DynaConfigPropertySettings;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DynaConfigDtoMapperTest {
  DynaConfigDtoMapper underTest;

  @BeforeEach
  void setUp() {
    PropertyDtoMapper propertyMapper = new PropertyDtoMapperImpl();
    InstancePropertyDtoMapper instanceInfosMapper = new InstancePropertyDtoMapperImpl();
    PropertyChangesDtoMapper changesMapper = new PropertyChangesDtoMapperImpl();
    IssuedCommandsDtoMapper commandsHistoryMapper = new IssuedCommandsDtoMapperImpl();

    underTest =
        new DynaConfigDtoMapper(
            propertyMapper, instanceInfosMapper, changesMapper, commandsHistoryMapper);
  }

  @Test
  void should_map_dynaconfig_dto() {
    DynaConfigDto dynaConfigDto = underTest.mapDynaConfigDto(
        fakeManifestSettings(), fakeDocuments(), fakeInstancesInfo(), fakeRequests());

    assertThat(dynaConfigDto.getProperties()).hasSize(2);
    PropertyDto propertyDto = dynaConfigDto.getProperties().get("propertyKey1");
    assertThat(propertyDto.getName()).isEqualTo("propertyKey1");
    assertThat(propertyDto.getValue()).isEqualTo("true");
    assertThat(propertyDto.getComment()).isEqualTo("Property comment");
    assertThat(propertyDto.getDescription()).isEqualTo("Property description");
    assertThat(propertyDto.getSource()).isEqualTo(Source.MANIFEST);
    assertThat(propertyDto.getStatus()).isEqualTo(Status.NOT_SYNCHRO);
    assertThat(propertyDto.getInstances()).hasSize(2);

    PropertyDto appPropertyDto = dynaConfigDto.getProperties().get("appProperty1");
    assertThat(appPropertyDto.getName()).isEqualTo("appProperty1");
    assertThat(appPropertyDto.getValue()).isEqualTo("123");
    assertThat(appPropertyDto.getSource()).isEqualTo(Source.APP);
  }

  private List<DynaConfigPropertySettings> fakeManifestSettings() {
    DynaConfigPropertySettings propSetting = new DynaConfigPropertySettings();
    propSetting.setDescription("Property description");
    propSetting.setName("propertyKey1");
    propSetting.setType("boolean");
    return List.of(propSetting);
  }

  private List<PropertyDocument> fakeDocuments() {
    PropertyDocument document = new PropertyDocument();
    document.setComment("Property comment");
    document.setName("propertyKey1");
    document.setValue("true");
    document.setHistory(
        Collections.singletonList(
            PropertyHistoryDocument.builder()
                .valueBeforeChange("false")
                .valueAfterChange("true")
                .build()));
    return List.of(document);
  }

  private List<DynaConfigInstance> fakeInstancesInfo() {
    DynaConfigInstance firstInstance = new DynaConfigInstance();
    firstInstance.setApplicationInstance("instance1");
    firstInstance.setCurrentValue("true");
    firstInstance.setPropertyKey("propertyKey1");

    DynaConfigInstance firstInstanceUnknownProp = new DynaConfigInstance();
    firstInstanceUnknownProp.setApplicationInstance("instance1");
    firstInstanceUnknownProp.setCurrentValue("123");
    firstInstanceUnknownProp.setPropertyKey("appProperty1");

    DynaConfigInstance secondInstance = new DynaConfigInstance();
    secondInstance.setApplicationInstance("instance2");
    secondInstance.setCurrentValue("false");
    secondInstance.setPropertyKey("propertyKey1");

    return List.of(firstInstance, firstInstanceUnknownProp, secondInstance);
  }

  private List<DynaConfigRequest> fakeRequests() {
    DynaConfigRequest request = new DynaConfigRequest();
    request.setRequestId("request1");
    request.setInstanceId("propertyKey1");
    request.setResponses(Collections.emptyList());
    return List.of(request);
  }
}
