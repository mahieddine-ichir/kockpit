package com.accor.wcp.console.services.dynaconfig.dynamo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.accor.wcp.console.services.dynaconfig.dto.PropertyDto;
import com.accor.wcp.console.services.dynaconfig.dto.PropertyInstanceDto;
import com.accor.wcp.console.services.dynaconfig.dto.Source;
import com.accor.wcp.console.services.dynaconfig.dto.Status;
import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigDocument;
import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigDocument.PropertyHistoryDocument;
import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigHistoryDocument;
import com.accor.wcp.console.services.dynaconfig.dynamo.repository.DynaConfigDocumentRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.accor.wcp.console.services.dynaconfig.dynamo.repository.DynaConfigHistoryDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DynaConfigDocumentServiceTest {
  public static final String DOMAIN_TEST = "domainTest";
  public static final String ENV_TEST = "envTest";
  public static final String APP_ID_TEST = "appIdTest";
  public static final String USERNAME = "test@user.com";

  @Mock private DynaConfigDocumentRepository repository;
  @Mock private DynaConfigHistoryDocumentRepository historyDocumentRepository;

  private DynaConfigDocumentService underTest;

  private DynaConfigDocument document;

  private static final Supplier<String> computeId = () -> DOMAIN_TEST + "-" + ENV_TEST + "-" + APP_ID_TEST;

  @BeforeEach
  void setup() {
    this.underTest = new DynaConfigDocumentService(repository, historyDocumentRepository);

    Map<String, DynaConfigDocument.PropertyDocument> propertiesValues = new HashMap<>();
    long lastUpdatedTimestamp = LocalDateTime.parse("2024-01-01T10:15:30").toEpochSecond(ZoneOffset.UTC);
    long historyLastUpdatedTimestamp = LocalDateTime.parse("2024-01-02T10:15:30").toEpochSecond(ZoneOffset.UTC);
    List<PropertyHistoryDocument> propertyHistoryDocuments = Stream.of(new PropertyHistoryDocument(historyLastUpdatedTimestamp, "oldValueTest", "valueTest", USERNAME))
            .collect(Collectors.toList());
    propertiesValues.put(
        "propertyTest",
        DynaConfigDocument.PropertyDocument.builder()
            .name("propertyTest")
            .value("valueTest")
            .comment("testComment")
            .lastUpdatedTimestamp(lastUpdatedTimestamp)
            .history(propertyHistoryDocuments)
            .build());

    this.document =
        DynaConfigDocument.builder()
            .id(computeId.get())
            .lastUpdatedTimestamp(new Date().getTime())
            .propertyValues(propertiesValues)
            .build();

    when(repository.findById(computeId.get()))
        .thenReturn(Optional.of(this.document));
  }

  @Test
  void assert_save_called_once() {

    PropertyInstanceDto propertyInstance =
        PropertyInstanceDto.builder()
            .applicationInstance("instanceIdTest")
            .status(Status.SYNCHRO)
            .currentValue("valueTest")
            .build();
    List<PropertyInstanceDto> instances = new ArrayList<>();
    instances.add(propertyInstance);

    Collection<PropertyDto> inputProps = new ArrayList<>();

    inputProps.add(
        PropertyDto.builder()
            .source(Source.APP)
            .lastUpdatedTimestamp(new Date().getTime())
            .name("propertyTest")
            .value("valueTestUpdate")
            .comment("commentTestUpdate")
            .status(Status.SYNCHRO)
            .instances(instances)
            .build());

    this.underTest.updateDocumentProperties(DOMAIN_TEST, ENV_TEST, APP_ID_TEST, inputProps, USERNAME);

    assertThat(this.document.getPropertyValues().get("propertyTest").getValue())
        .isEqualTo("valueTestUpdate");
    assertThat(this.document.getPropertyValues().get("propertyTest").getComment())
        .isEqualTo("commentTestUpdate");
    assertThat(this.document.getPropertyValues().get("propertyTest").getHistory())
        .hasSize(2);

    verify(repository, times(1)).save(this.document);
  }

  @Test
  void should_update_property() {
    underTest.updateDocumentProperty(
        DOMAIN_TEST, ENV_TEST, APP_ID_TEST, "propertyTest", "shouldUpdate");

    verify(repository, times(1)).save(this.document);

    assertThat(this.document.getPropertyValues().get("propertyTest").getValue())
        .isEqualTo("shouldUpdate");
  }

  @Test
  void should_remove_property() {
    underTest.removeDocumentProperty(DOMAIN_TEST, ENV_TEST, APP_ID_TEST, "propertyTest");

    verify(repository, times(1)).save(this.document);

    assertThat(this.document.getPropertyValues()).isEmpty();
  }

  @Test
  void flushHistory_ShouldCopyHistoryAndUpdateProperties() {
    // Call the method to test
    underTest.flushHistory(DOMAIN_TEST, ENV_TEST, APP_ID_TEST);

    // Verify that history was saved
    ArgumentCaptor<DynaConfigHistoryDocument> historyCaptor = ArgumentCaptor.forClass(DynaConfigHistoryDocument.class);
    verify(historyDocumentRepository, times(1)).save(historyCaptor.capture());

    List<DynaConfigHistoryDocument> savedHistories = historyCaptor.getAllValues();
    assertThat(savedHistories).hasSize(1);
    assertThat(savedHistories.get(0).getId()).isEqualTo(computeId.get() + "-" + "propertyTest");

    // Verify that the updated document was saved
    ArgumentCaptor<DynaConfigDocument> documentCaptor = ArgumentCaptor.forClass(DynaConfigDocument.class);
    verify(repository, times(1)).save(documentCaptor.capture());

    DynaConfigDocument savedDocument = documentCaptor.getValue();
    assertThat(savedDocument.getPropertyValues()).hasSize(1);
    assertThat(savedDocument.getPropertyValues().get("propertyTest").getName()).isEqualTo("propertyTest");
    assertThat(savedDocument.getPropertyValues().get("propertyTest").getValue()).isEqualTo("valueTest");
  }

}
