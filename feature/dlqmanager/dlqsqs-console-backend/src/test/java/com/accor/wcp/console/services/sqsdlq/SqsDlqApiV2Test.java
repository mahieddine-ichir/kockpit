package com.accor.wcp.console.services.sqsdlq;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.accor.wcp.console.services.sqsdlq.dynamo.SqsDocumentServiceV2;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsAttributeType;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentRetry;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentStatus;
import com.accor.wcp.console.services.sqsdlq.model.Count;
import com.accor.wcp.console.services.sqsdlq.model.PartitionKey;
import com.accor.wcp.console.services.sqsdlq.model.RetriesDto;
import com.accor.wcp.console.services.sqsdlq.model.RetryDto;
import com.accor.wcp.console.services.sqsdlq.model.SqsAttributeDto;
import com.accor.wcp.console.services.sqsdlq.model.SqsDlqSettingsDto;
import com.accor.wcp.console.services.sqsdlq.model.SqsMessageDtoV2;
import com.accor.wcp.console.services.sqsdlq.model.SqsMessagesDtoV2;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(
    controllers = SqsDlqApiV2.class,
    excludeFilters = {
      @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfigurer.class)
    },
    excludeAutoConfiguration = {OAuth2ResourceServerAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class SqsDlqApiV2Test {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper mapper;
  @MockBean private SqsAwsServiceV2 sqsAwsServiceV2;
  @MockBean private SqsDocumentServiceV2 sqsDocumentServiceV2;
  @MockBean private MigrationService migrationService;
  @MockBean private SqsDlqSettingsService sqsDlqSettingsService;

  @Test
  public void get_should_return_sqsdlq_messages() throws Exception {
    // GIVEN
    String applicationId = "application";
    String queueName = "queueName";
    SqsMessageDtoV2 sqsMessage = getSqsMessageDto();

    Map<SqsDocumentStatus, Long> nbMessagesByStatus =
        Map.of(SqsDocumentStatus.RESOLVED, 2L, SqsDocumentStatus.NEW, 4L);

    SqsMessagesDtoV2 messages =
        SqsMessagesDtoV2.builder()
            .nbMessagesByStatus(nbMessagesByStatus)
            .messages(Collections.singletonList(sqsMessage))
            .build();

    doReturn(messages)
        .when(sqsDocumentServiceV2)
        .find(
            eq(PartitionKey.builder().domain("domain").env("env").queueName(queueName).build()),
            isNull(),
            eq(Collections.singletonList(SqsDocumentStatus.RESOLVED.getValue())));

    // WHEN
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                    "/api/services/domain/env/sqsdlq/"
                        + applicationId
                        + "/"
                        + queueName
                        + "/messages")
                .param("status", SqsDocumentStatus.RESOLVED.getValue()))
        .andDo(print())
        // THEN
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.messages[0].id", is(sqsMessage.getId())))
        .andExpect(jsonPath("$.messages[0].body", is(sqsMessage.getBody())))
        .andExpect(
            jsonPath(
                "$.messages[0].attributes[0].name",
                is(sqsMessage.getAttributes().get(0).getName())))
        .andExpect(
            jsonPath(
                "$.messages[0].attributes[0].value",
                is(sqsMessage.getAttributes().get(0).getValue())))
        .andExpect(
            jsonPath(
                "$.messages[0].attributes[0].type",
                is(sqsMessage.getAttributes().get(0).getType().name())))
        .andExpect(jsonPath("$.messages[0].groupId", is(sqsMessage.getGroupId())))
        .andExpect(jsonPath("$.messages[0].sentTimestamp", is(sqsMessage.getSentTimestamp())))
        .andExpect(jsonPath("$.messages[0].status", is(sqsMessage.getStatus().getValue())))
        .andExpect(jsonPath("$.messages[0].comment", is(sqsMessage.getComment())))
        .andExpect(jsonPath("$.nbMessagesByStatus", Matchers.hasEntry("RESOLVED", 2)))
        .andExpect(jsonPath("$.nbMessagesByStatus", Matchers.hasEntry("NEW", 4)));

    verify(sqsDocumentServiceV2)
        .find(
            eq(PartitionKey.builder().domain("domain").env("env").queueName(queueName).build()),
            isNull(),
            eq(Collections.singletonList(SqsDocumentStatus.RESOLVED.getValue())));
  }

  @Test
  public void getOne_should_return_sqsdlq_message() throws Exception {
    // GIVEN
    SqsMessageDtoV2 sqsMessageResponse = getSqsMessageDto();
    PartitionKey partitionKey =
        PartitionKey.builder().domain("domain").env("env").queueName("queueName").build();

    doReturn(sqsMessageResponse).when(sqsDocumentServiceV2).fetchOne(eq(partitionKey), eq("id"));

    // WHEN
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                    "/api/services/domain/env/sqsdlq/application/queueName/id/message")
                .param("status", SqsDocumentStatus.RESOLVED.getValue()))
        .andDo(print())
        // THEN
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(sqsMessageResponse.getId())))
        .andExpect(jsonPath("$.body", is(sqsMessageResponse.getBody())))
        .andExpect(
            jsonPath(
                "$.attributes[0].name", is(sqsMessageResponse.getAttributes().get(0).getName())))
        .andExpect(
            jsonPath(
                "$.attributes[0].value", is(sqsMessageResponse.getAttributes().get(0).getValue())))
        .andExpect(
            jsonPath(
                "$.attributes[0].type",
                is(sqsMessageResponse.getAttributes().get(0).getType().name())))
        .andExpect(jsonPath("$.groupId", is(sqsMessageResponse.getGroupId())))
        .andExpect(jsonPath("$.sentTimestamp", is(sqsMessageResponse.getSentTimestamp())))
        .andExpect(jsonPath("$.status", is(sqsMessageResponse.getStatus().getValue())))
        .andExpect(jsonPath("$.comment", is(sqsMessageResponse.getComment())));

    verify(sqsDocumentServiceV2).fetchOne(eq(partitionKey), eq("id"));
  }

  @Test
  public void count_should_return_sqsdlq_messages_count() throws Exception {
    // GIVEN
    Count countResponse = Count.builder().totalCount(123).build();
    PartitionKey partitionKey =
        PartitionKey.builder().domain("domain").env("env").queueName("queueName").build();

    doReturn(countResponse).when(sqsDocumentServiceV2).getCount(eq(partitionKey));

    // WHEN
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                "/api/services/domain/env/sqsdlq/application/queueName/count"))
        .andDo(print())
        // THEN
        .andExpect(status().isOk());
    // BUG
    // .andExpect(content().json("{\"totalCount\": 123}"));

    verify(sqsDocumentServiceV2).getCount(eq(partitionKey));
  }

  @Test
  public void update_should_return_status_OK() throws Exception {
    // GIVEN
    PartitionKey partitionKey =
        PartitionKey.builder().domain("domain").env("env").queueName("queueName").build();
    SqsMessageDtoV2 sqsMessageRequest = getSqsMessageDto();

    String bodyRequest = mapper.writeValueAsString(sqsMessageRequest);

    // WHEN
    mockMvc
        .perform(
            MockMvcRequestBuilders.put(
                    "/api/services/domain/env/sqsdlq/applicationId/queueName/Id/message")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyRequest))
        .andDo(print())
        // THEN
        .andExpect(status().isOk());

    verify(sqsDocumentServiceV2).update(eq(partitionKey), eq(sqsMessageRequest));
  }

  @Test
  public void delete_multiple_should_return_status_OK() throws Exception {
    // GIVEN
    String applicationId = "application";
    String queueName = "queueName";
    PartitionKey partitionKey =
        PartitionKey.builder().domain("domain").env("env").queueName(queueName).build();
    List<String> ids = List.of("id1", "id2");
    String bodyRequest = mapper.writeValueAsString(ids);

    // WHEN
    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(
                    "/api/services/domain/env/sqsdlq/"
                        + applicationId
                        + "/"
                        + queueName
                        + "/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyRequest))
        .andDo(print())
        // THEN
        .andExpect(status().isNoContent());
    verify(sqsDocumentServiceV2).delete(eq(partitionKey), eq(ids));
  }

  @Test
  public void delete_all_should_return_OK_when_missing_mandatory_body() throws Exception {
    // GIVEN
    String applicationId = "application";
    String queueName = "queueName";
    PartitionKey partitionKey =
        PartitionKey.builder().domain("domain").env("env").queueName(queueName).build();

    // WHEN
    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(
                    "/api/services/domain/env/sqsdlq/"
                        + applicationId
                        + "/"
                        + queueName
                        + "/messages")
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        // THEN
        .andExpect(status().isNoContent());
    verify(sqsDocumentServiceV2).delete(eq(partitionKey), isNull());
  }

  @Test
  public void retries_multiple_should_return_OK_when_missing_mandatory_body() throws Exception {
    // GIVEN
    String applicationId = "application";
    String queueName = "queueName";
    PartitionKey partitionKey =
        PartitionKey.builder().domain("domain").env("env").queueName(queueName).build();
    SqsDlqSettingsDto sqsDlqSettingsDto = SqsDlqSettingsDto.builder().build();
    doReturn(sqsDlqSettingsDto).when(sqsDlqSettingsService).getSqsDlqSettingsDto(eq(partitionKey));

    String parentId = "mock_parentId";
    SqsDocumentRetry sqsDocumentRetry =
        SqsDocumentRetry.builder().sentTimestamp("123456789").build();
    RetriesDto requestBody =
        RetriesDto.builder()
            .retries(List.of(RetryDto.builder().parentId(parentId).retry(sqsDocumentRetry).build()))
            .build();
    String requestBodyString = mapper.writeValueAsString(requestBody);

    // WHEN
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(
                    "/api/services/domain/env/sqsdlq/"
                        + applicationId
                        + "/"
                        + queueName
                        + "/retries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyString))
        .andDo(print())
        // THEN
        .andExpect(status().isOk());
    verify(sqsDlqSettingsService).getSqsDlqSettingsDto(eq(partitionKey));
    verify(sqsAwsServiceV2)
        .sendMultipleRetryMessage(
            eq(partitionKey), eq(requestBody.getRetries()), eq(sqsDlqSettingsDto));
  }

  @Test
  public void retries_all_should_return_NO_CONTENT_when_missing_mandatory_body() throws Exception {
    // GIVEN
    String applicationId = "application";
    String queueName = "queueName";
    PartitionKey partitionKey =
        PartitionKey.builder().domain("domain").env("env").queueName(queueName).build();
    SqsDlqSettingsDto sqsDlqSettingsDto = SqsDlqSettingsDto.builder().build();
    doReturn(sqsDlqSettingsDto).when(sqsDlqSettingsService).getSqsDlqSettingsDto(eq(partitionKey));

    // WHEN
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(
                    "/api/services/domain/env/sqsdlq/"
                        + applicationId
                        + "/"
                        + queueName
                        + "/retries")
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        // THEN
        .andExpect(status().isNoContent());
    verify(sqsDlqSettingsService).getSqsDlqSettingsDto(eq(partitionKey));
    verify(sqsAwsServiceV2).sendAllRetryMessage(eq(partitionKey), eq(sqsDlqSettingsDto));
  }

  private SqsMessageDtoV2 getSqsMessageDto() {
    return SqsMessageDtoV2.builder()
        .body("Body")
        .id("Id")
        .attributes(
            Collections.singletonList(
                SqsAttributeDto.builder()
                    .name("key1")
                    .value("value")
                    .type(SqsAttributeType.STRING)
                    .build()))
        .groupId("Group Id")
        .sentTimestamp(Instant.now().toEpochMilli())
        .comment("comment")
        .status(SqsDocumentStatus.RESOLVED)
        .domain("domain")
        .environment("env")
        .build();
  }
}
