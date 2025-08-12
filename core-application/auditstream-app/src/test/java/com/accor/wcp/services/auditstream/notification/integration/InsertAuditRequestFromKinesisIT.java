package com.accor.wcp.services.auditstream.notification.integration;

import static com.accor.wcp.services.auditstream.notification.integration.IntegrationTestUtils.buildAuditReportRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.accor.wcp.console.services.audit.kengine.dynamodb.KEngineRegistryDocument;
import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.SharedBaseIt;
import com.accor.wcp.services.auditstream.notification.es.TestEsHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.opensearch.OpenSearchStatusException;
import org.opensearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.org.apache.commons.lang3.SerializationUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

class InsertAuditRequestFromKinesisIT extends SharedBaseIt {

  @Autowired ObjectMapper objectMapper;

  @Autowired TestEsHelper testEsHelper;

  private static final String AUDIT_STREAM_NAME = "audit-it";

  @Test
  void should_not_report_same_audit_request_twice() throws Exception {
    AuditReportRequest auditReportRequest1 = buildAuditReportRequest("audit-sample1-full.json");
    AuditReportRequest auditReportRequest2 = SerializationUtils.clone(auditReportRequest1);
    String domain = auditReportRequest1.getDomain();
    String env = auditReportRequest1.getEnv();

    putKinesisRecord(kinesisAsyncClient, auditReportRequest1);
    testEsHelper.waitForIndexInitialized();
    putKinesisRecord(kinesisAsyncClient, auditReportRequest2);

    List<SearchHit> searchHits = testEsHelper.getAllSearchHits(domain, env);
    assertThat(searchHits).hasSize(1);
  }

  @Test
  void should_write_alias() throws Exception {
    AuditReportRequest auditReportRequest = buildAuditReportRequest("audit-sample1-full.json");
    String domain = auditReportRequest.getDomain();
    String env = auditReportRequest.getEnv();

    putKinesisRecord(kinesisAsyncClient, auditReportRequest);
    testEsHelper.waitForIndexInitialized();

    assertThat(testEsHelper.getAliasWriteAlreadyInit()).hasSize(1);
    String alias = testEsHelper.getAliasWriteAlreadyInit().stream().findFirst().get();
    assertThat(alias).contains(domain);
    assertThat(alias).contains(env);
    // Default ttl
    assertThat(alias).contains("ttl30");
  }

  @Test
  void should_write_document_in_dynamodb() throws Exception {
    AuditReportRequest auditReportRequest = buildAuditReportRequest("audit-sample1-full.json");

    putKinesisRecord(kinesisAsyncClient, auditReportRequest);
    testEsHelper.waitForIndexInitialized();

    PageIterable<KEngineRegistryDocument> scanResult = dynamoDbTable.scan();
    List<KEngineRegistryDocument> kEngineRegistryDocuments = scanResult.items().stream().toList();

    assertThat(kEngineRegistryDocuments).hasSize(1);
  }

  @Test
  void should_not_report_audit_request_without_domain_or_env() throws Exception {
    AuditReportRequest auditReportRequest = buildAuditReportRequest("audit-sample1-optimized.json");
    String env = auditReportRequest.getEnv();
    auditReportRequest.setDomain(null);

    putKinesisRecord(kinesisAsyncClient, auditReportRequest);

    assertThrows(
        OpenSearchStatusException.class, () -> testEsHelper.getLastOpenSearchDocument(null, env));
  }

  public void putKinesisRecord(
      KinesisAsyncClient kinesisAsyncClient, AuditReportRequest auditReportRequest)
      throws JsonProcessingException, ExecutionException, InterruptedException {

    PutRecordRequest putRecordRequest =
        PutRecordRequest.builder()
            .partitionKey(UUID.randomUUID().toString())
            .streamName(AUDIT_STREAM_NAME)
            .data(
                SdkBytes.fromByteArray(
                    objectMapper.writeValueAsString(auditReportRequest).getBytes()))
            .build();

    kinesisAsyncClient.putRecord(putRecordRequest).get();
  }
}
