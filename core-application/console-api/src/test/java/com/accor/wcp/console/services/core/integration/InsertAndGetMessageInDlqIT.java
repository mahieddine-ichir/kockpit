package com.accor.wcp.console.services.core.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.accor.wcp.console.services.core.appmanifest.manifest.Manifest;
import com.accor.wcp.console.services.core.appmanifest.s3.datasource.ResourceLoaderS3;
import com.accor.wcp.console.services.core.integration.utils.DedicatedBaseIt;
import com.accor.wcp.console.services.sqk.TestUtils;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsAttributeType;
import com.accor.wcp.console.services.sqsdlq.model.PartitionKey;
import com.accor.wcp.console.services.sqsdlq.model.SqsAttributeDto;
import com.accor.wcp.console.services.sqsdlq.model.SqsMessageDtoV2;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
class InsertAndGetMessageInDlqIT extends DedicatedBaseIt {

  @Autowired MockMvc mockMvc;

  @TestConfiguration
  public static class InitDlqITConfiguration {
    // test doesn't launch without these beans
    @MockBean
    RestHighLevelClient restHighLevelClient;
    @MockBean JwtDecoder jwtDecoder;
    @MockBean
    ResourceLoaderS3 resourceLoaderS3;

    @PostConstruct
    public void initMock() {
      List<Manifest> manifests = TestUtils.loadFakeSqsDlqManifests();
      when(resourceLoaderS3.getBucketObjects()).thenReturn(manifests);
    }
  }

  @Test
  void should_insert_message_in_dlq_and_return_200() throws Exception {
    SqsMessageDtoV2 sqsMessage = buildSqsMessageDtoV2();
    String queueUrl = sqsClient.listQueues().queueUrls().get(0);

    sqsClient.sendMessage(
        SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageDeduplicationId("dedupid")
            .messageGroupId("admin")
            .messageBody("mock_body")
            .build());

    // Wait 1s (wait for sqs message)
    Thread.sleep(1000);

    performGetSqsdlqMessages(sqsMessage);
  }

  private void performGetSqsdlqMessages(SqsMessageDtoV2 sqsMessage) throws Exception {
    mockMvc
        .perform(get("/api/services/WCXSS/test/sqsdlq/wcxss-insurance/testdlq.fifo/messages", 42L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nbMessagesByStatus.NEW", is(1)))
        .andExpect(jsonPath("$.messages[0].body", is(sqsMessage.getBody())))
        .andExpect(jsonPath("$.messages[0].partitionKey", is(sqsMessage.getPartitionKey())))
        .andExpect(jsonPath("$.messages[0].groupId", is(sqsMessage.getGroupId())))
        .andDo(print());
  }

  private static SqsMessageDtoV2 buildSqsMessageDtoV2() {
    PartitionKey partitionKey =
        PartitionKey.builder().domain("WCXSS").env("test").queueName("testdlq.fifo").build();
    String body = "mock_body";

    return SqsMessageDtoV2.builder()
        .id("1")
        .partitionKey(partitionKey.toString())
        .body(body)
        .attributes(
            Arrays.asList(
                SqsAttributeDto.builder()
                    .name("keyString")
                    .value("value")
                    .type(SqsAttributeType.STRING)
                    .build(),
                SqsAttributeDto.builder()
                    .name("keyNumber")
                    .value("value")
                    .type(SqsAttributeType.NUMBER)
                    .build()))
        .groupId("admin")
        .domain("WCXSS")
        .environment("test")
        .build();
  }
}
