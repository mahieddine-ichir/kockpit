package com.accor.wcp.console.services.sqsdlq.dynamo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accor.wcp.console.services.sqsdlq.NotFoundException;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.AttributeDocument;
import com.accor.wcp.console.services.sqsdlq.SqsDlqSettingsService;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsAttributeType;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocument;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentStatus;
import com.accor.wcp.console.services.sqsdlq.mapper.SqsMessageMapper;
import com.accor.wcp.console.services.sqsdlq.mapper.SqsMessageMapperImpl;
import com.accor.wcp.console.services.sqsdlq.model.SqsAttributeDto;
import com.accor.wcp.console.services.sqsdlq.model.SqsDlqSettingsDto;
import com.accor.wcp.console.services.sqsdlq.model.SqsMessageDto;
import com.accor.wcp.console.services.sqsdlq.model.SqsMessagesDto;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SqsDocumentServiceTest {

  private SqsDocumentRepository sqsDocumentRepository;
  private SqsDocumentService sqsDocumentService;
  private SqsDlqSettingsService sqsDlqSettingsService;

  @BeforeEach
  void init(
      @Mock SqsDocumentRepository sqsDocumentRepository,
      @Mock SqsDlqSettingsService sqsDlqSettingsService) {
    this.sqsDocumentRepository = sqsDocumentRepository;
    this.sqsDlqSettingsService = sqsDlqSettingsService;
    sqsDocumentService =
        new SqsDocumentService(
            sqsDocumentRepository, new SqsMessageMapperImpl(), sqsDlqSettingsService);
  }

  @Test
  public void should_find_by_queue_name_and_filter_by_status() {
    // GIVEN
    SqsDocument sqsDocumentResolved = createSqsDocument();
    SqsDocument sqsDocumentNew = SqsDocument.builder().status(SqsDocumentStatus.NEW).build();

    when(sqsDocumentRepository.findAllByExpression(
            "queueName", "domain", "env", List.of("RESOLVED")))
        .thenReturn(Collections.singletonList(sqsDocumentResolved));

    // WHEN
    SqsMessagesDto sqsMessagesDto =
        sqsDocumentService.findByQueueNameAndStatus(
            "domain",
            "env",
            "applicationId",
            "queueName",
            Collections.singletonList(SqsDocumentStatus.RESOLVED.getValue()));

    // THEN
    assertThat(sqsMessagesDto.getMessages()).hasSize(1);
    check(sqsMessagesDto.getMessages().get(0), sqsDocumentResolved);

    assertThat(sqsMessagesDto.getNbMessagesByStatus().get(SqsDocumentStatus.RESOLVED))
        .isEqualTo(1L);
  }

  @Test
  public void should_find_by_queue_name_and_not_filter_by_status() {
    // GIVEN
    SqsDocument sqsDocumentResolved =
        SqsDocument.builder().status(SqsDocumentStatus.RESOLVED).build();

    SqsDocument sqsDocumentNew = SqsDocument.builder().status(SqsDocumentStatus.NEW).build();

    when(sqsDocumentRepository.findAllByExpression(
            "queueName", "domain", "env", Collections.emptyList()))
        .thenReturn(Arrays.asList(sqsDocumentResolved, sqsDocumentNew));

    // WHEN
    SqsMessagesDto sqsMessagesDto =
        sqsDocumentService.findByQueueNameAndStatus(
            "domain", "env", "applicationId", "queueName", Collections.EMPTY_LIST);

    // THEN
    assertThat(sqsMessagesDto.getMessages()).hasSize(2);
  }

  @Test
  public void should_find_by_queue_can_return_empty_list() {
    // GIVEN
    when(sqsDocumentRepository.findAllByExpression(
            "queueName", "domain", "env", Collections.emptyList()))
        .thenReturn(Collections.emptyList());

    // WHEN
    SqsMessagesDto sqsMessagesDto =
        sqsDocumentService.findByQueueNameAndStatus(
            "domain", "env", "applicationId", "queueName", Collections.EMPTY_LIST);

    // THEN
    assertThat(sqsMessagesDto.getMessages()).hasSize(0);
  }

  @Test
  void should_find_by_queueName_domain_environment_and_status() {
    // Given
    SqsDocument sqsDocumentWithoutDomainOrEnvironment = createSqsDocument();
    SqsDocument sqsDocumentWithDomainOrEnvironment = createSqsDocumentWithDomainAndEnvironment();

    when(sqsDocumentRepository.findAllByExpression(
            "queueName", "domain", "env", List.of("RESOLVED", "NEW")))
        .thenReturn(
            Arrays.asList(
                sqsDocumentWithoutDomainOrEnvironment, sqsDocumentWithDomainOrEnvironment));

    // When
    SqsMessagesDto sqsMessagesDto =
        sqsDocumentService.findByQueueNameAndStatus(
            "domain",
            "env",
            "applicationId",
            "queueName",
            List.of(SqsDocumentStatus.RESOLVED.getValue(), SqsDocumentStatus.NEW.getValue()));

    // Then
    assertThat(sqsMessagesDto.getMessages()).hasSize(2);
    assertThat(sqsMessagesDto.getMessages().get(0).getDomain()).isNull();
    assertThat(sqsMessagesDto.getMessages().get(0).getEnvironment()).isNull();
    assertThat(sqsMessagesDto.getMessages().get(1).getDomain()).isEqualTo("domain");
    assertThat(sqsMessagesDto.getMessages().get(1).getEnvironment()).isEqualTo("env");
  }

  @Test
  public void should_delete_by_ids() {
    // GIVEN
    List<String> ids = Arrays.asList("id1", "id2");

    // WHEN
    String applicationId = "applicationId";
    String queueName = "queueName";
    sqsDocumentService.deleteByIds("domain", "env", applicationId, queueName, ids);

    // THEN
    ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
    verify(sqsDocumentRepository, times(2)).deleteById(argument.capture());
    assertThat(argument.getAllValues()).containsAll(ids);
  }

  @Test
  public void should_find_by_id() throws NotFoundException {
    // GIVEN
    SqsDocument sqsDoc = createSqsDocument();
    when(sqsDocumentRepository.findById(sqsDoc.getId())).thenReturn(Optional.of(sqsDoc));

    // WHEN
    SqsMessageDto sqsMessageDto =
        sqsDocumentService.findById("domain", "env", "applicationId", "queueName", sqsDoc.getId());

    // THEN
    check(sqsMessageDto, sqsDoc);
  }

  @Test
  public void should_find_by_id_throw_exception_when_id_exist() {
    assertThrows(
        NotFoundException.class,
        () -> sqsDocumentService.findById("domain", "env", "applicationId", "queueName", "Id"));
  }

  @Test
  public void should_update() throws NotFoundException {
    // GIVEN
    Instant now = Instant.now();
    String id = "ID";
    SqsMessageDto sqsMessagesDto =
        SqsMessageDto.builder()
            .body("Body")
            .groupId("GroupId")
            .attributes(
                Collections.singletonList(
                    SqsAttributeDto.builder()
                        .name("key1")
                        .value("value")
                        .type(SqsAttributeType.STRING)
                        .build()))
            .sentTimestamp(Instant.now().toEpochMilli())
            .comment("comment")
            .status(SqsDocumentStatus.RESOLVED)
            .build();

    when(sqsDocumentRepository.findById(id)).thenReturn(Optional.of(SqsDocument.builder().build()));
    initSqsDlqSettingsServiceMock("env", "queueName", "applicationId", null);

    // WHEN
    sqsDocumentService.update("domain", "env", "applicationId", "queueName", id, sqsMessagesDto);

    // THEN
    ArgumentCaptor<SqsDocument> argument = ArgumentCaptor.forClass(SqsDocument.class);
    verify(sqsDocumentRepository, times(1)).update(argument.capture());

    SqsDocument sqsDocArg = argument.getValue();

    assertThat(sqsDocArg.getBody()).isEqualTo(sqsMessagesDto.getBody());
    assertThat(sqsDocArg.getId()).isEqualTo(id);
    assertThat(sqsDocArg.getGroupId()).isEqualTo(sqsMessagesDto.getGroupId());
    assertThat(sqsDocArg.getStatus()).isEqualTo(sqsMessagesDto.getStatus());
    assertThat(sqsDocArg.getComment()).isEqualTo(sqsMessagesDto.getComment());
    assertThat(sqsDocArg.getAttributes().get(0).getValue())
        .isEqualTo(sqsMessagesDto.getAttributes().get(0).getValue());
    assertThat(sqsDocArg.getAttributes().get(0).getName())
        .isEqualTo(sqsMessagesDto.getAttributes().get(0).getName());
    assertThat(sqsDocArg.getAttributes().get(0).getType())
        .isEqualTo(sqsMessagesDto.getAttributes().get(0).getType());
    assertThat(sqsDocArg.getSentTimestamp()).isEqualTo(sqsMessagesDto.getSentTimestamp());
  }

  @Test
  public void should_update_with_default_ttl_if_resolved() throws NotFoundException {
    // GIVEN
    SqsMessageDto sqsMessagesDto =
        SqsMessageDto.builder()
            .status(SqsDocumentStatus.RESOLVED)
            .sentTimestamp(Instant.now().toEpochMilli())
            .build();

    when(sqsDocumentRepository.findById("id"))
        .thenReturn(Optional.of(SqsDocument.builder().build()));
    initSqsDlqSettingsServiceMock("env", "queueName", "applicationId", null);

    // WHEN
    sqsDocumentService.update("domain", "env", "applicationId", "queueName", "id", sqsMessagesDto);

    // THEN
    ArgumentCaptor<SqsDocument> argument = ArgumentCaptor.forClass(SqsDocument.class);
    verify(sqsDocumentRepository, times(1)).update(argument.capture());

    SqsDocument sqsDocArg = argument.getValue();
    assertThat(sqsDocArg.getEvictionDateTime())
        .isEqualTo(
            sqsMessagesDto.getSentTimestamp()
                + SqsMessageMapper.DEFAULT_TTL_IN_DAYS * 24L * 3600 * 1000);
  }

  @Test
  public void should_update_with_manifest_ttl_if_resolved() throws NotFoundException {
    // GIVEN
    Integer manifestTtl = 30;
    SqsMessageDto sqsMessagesDto =
        SqsMessageDto.builder()
            .status(SqsDocumentStatus.RESOLVED)
            .sentTimestamp(Instant.now().toEpochMilli())
            .build();

    when(sqsDocumentRepository.findById("id"))
        .thenReturn(Optional.of(SqsDocument.builder().build()));
    initSqsDlqSettingsServiceMock("env", "queueName", "applicationId", manifestTtl);

    // WHEN
    sqsDocumentService.update("domain", "env", "applicationId", "queueName", "id", sqsMessagesDto);

    // THEN
    ArgumentCaptor<SqsDocument> argument = ArgumentCaptor.forClass(SqsDocument.class);
    verify(sqsDocumentRepository, times(1)).update(argument.capture());

    SqsDocument sqsDocArg = argument.getValue();
    assertThat(sqsDocArg.getEvictionDateTime())
        .isEqualTo(sqsMessagesDto.getSentTimestamp() + manifestTtl * 24L * 3600 * 1000);
  }

  @Test
  public void should_update_not_set_ttl_if_not_resolved() throws NotFoundException {
    // GIVEN
    SqsMessageDto sqsMessagesDto = SqsMessageDto.builder().status(SqsDocumentStatus.NEW).build();

    when(sqsDocumentRepository.findById("id"))
        .thenReturn(Optional.of(SqsDocument.builder().build()));
    initSqsDlqSettingsServiceMock("env", "queueName", "applicationId", null);

    // WHEN
    sqsDocumentService.update("domain", "env", "applicationId", "queueName", "id", sqsMessagesDto);

    // THEN
    ArgumentCaptor<SqsDocument> argument = ArgumentCaptor.forClass(SqsDocument.class);
    verify(sqsDocumentRepository, times(1)).update(argument.capture());

    SqsDocument sqsDocArg = argument.getValue();
    assertThat(sqsDocArg.getEvictionDateTime()).isNull();
  }

  @Test
  public void should_update_throw_exception_when_id_not_exist() {
    assertThrows(
        NotFoundException.class,
        () ->
            sqsDocumentService.update(
                "domain",
                "env",
                "applicationId",
                "queueName",
                "Id",
                SqsMessageDto.builder().build()));
  }

  private SqsDocument createSqsDocument() {
    return SqsDocument.builder()
        .id("Id")
        .body("Body")
        .groupId("GroupId")
        .attributes(
            Collections.singletonList(
                AttributeDocument.builder()
                    .name("key1")
                    .value("value")
                    .type(SqsAttributeType.STRING)
                    .build()))
        .sentTimestamp(Instant.now().toEpochMilli())
        .comment("comment")
        .status(SqsDocumentStatus.RESOLVED)
        .build();
  }

  private SqsDocument createSqsDocumentWithDomainAndEnvironment() {
    return SqsDocument.builder()
        .id("Id")
        .body("Body")
        .groupId("GroupId")
        .attributes(
            Collections.singletonList(
                AttributeDocument.builder()
                    .name("key1")
                    .value("value")
                    .type(SqsAttributeType.STRING)
                    .build()))
        .sentTimestamp(Instant.now().toEpochMilli())
        .comment("comment")
        .status(SqsDocumentStatus.NEW)
        .domain("domain")
        .environment("env")
        .build();
  }

  private void check(SqsMessageDto actual, SqsDocument expected) {
    assertThat(actual.getBody()).isEqualTo(expected.getBody());
    assertThat(actual.getId()).isEqualTo(expected.getId());
    assertThat(actual.getGroupId()).isEqualTo(expected.getGroupId());
    assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
    assertThat(actual.getComment()).isEqualTo(expected.getComment());
    assertThat(actual.getAttributes().get(0).getValue())
        .isEqualTo(expected.getAttributes().get(0).getValue());
    assertThat(actual.getAttributes().get(0).getName())
        .isEqualTo(expected.getAttributes().get(0).getName());
    assertThat(actual.getAttributes().get(0).getType())
        .isEqualTo(expected.getAttributes().get(0).getType());
    assertThat(actual.getSentTimestamp()).isEqualTo(expected.getSentTimestamp());
  }

  @SneakyThrows
  private void initSqsDlqSettingsServiceMock(String env, String name, String appId, Integer ttl) {
    SqsDlqSettingsDto queueSetting = new SqsDlqSettingsDto();
    queueSetting.setEnv(env);
    queueSetting.setName(name);
    queueSetting.setTtl(ttl);
    when(sqsDlqSettingsService.getSqsDlqSettingsDto(env, name, appId)).thenReturn(queueSetting);
  }
}
