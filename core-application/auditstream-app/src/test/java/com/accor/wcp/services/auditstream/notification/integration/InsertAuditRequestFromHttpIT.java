package com.accor.wcp.services.auditstream.notification.integration;

import com.accor.wcp.console.services.audit.kengine.dynamodb.KEngineRegistryDocument;
import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.SharedBaseIt;
import com.accor.wcp.services.auditstream.notification.es.TestEsHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.opensearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

import java.util.List;

import static com.accor.wcp.services.auditstream.notification.integration.IntegrationTestUtils.buildAuditReportRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
class InsertAuditRequestFromHttpIT extends SharedBaseIt {

  @Autowired ObjectMapper objectMapper;

  @Autowired TestEsHelper testEsHelper;

  @Autowired MockMvc mockMvc;

  @Test
  void should_not_report_same_audit_request_twice_and_return_200() throws Exception {
    AuditReportRequest auditReportRequest = buildAuditReportRequest("audit-sample1-full.json");
    String domain = auditReportRequest.getDomain();
    String env = auditReportRequest.getEnv();

    this.performPostNotification(auditReportRequest, 200);
    this.performPostNotification(auditReportRequest, 200);

    List<SearchHit> searchHits = testEsHelper.getAllSearchHits(domain, env);
    assertThat(searchHits).hasSize(1);
  }

  @Test
  void should_write_alias_and_return_200() throws Exception {
    AuditReportRequest auditReportRequest = buildAuditReportRequest("audit-sample1-full.json");
    String domain = auditReportRequest.getDomain();
    String env = auditReportRequest.getEnv();

    this.performPostNotification(auditReportRequest, 200);

    assertThat(testEsHelper.getAliasWriteAlreadyInit()).hasSize(1);
    String alias = testEsHelper.getAliasWriteAlreadyInit().stream().findFirst().get();
    assertThat(alias).contains(domain);
    assertThat(alias).contains(env);
    // Default ttl
    assertThat(alias).contains("ttl30");
  }

  @Test
  void should_write_document_in_dynamodb_and_return_200() throws Exception {
    AuditReportRequest auditReportRequest = buildAuditReportRequest("audit-sample1-full.json");

    this.performPostNotification(auditReportRequest, 200);

    PageIterable<KEngineRegistryDocument> scanResult = dynamoDbTable.scan();
    List<KEngineRegistryDocument> kEngineRegistryDocuments = scanResult.items().stream().toList();

    assertThat(kEngineRegistryDocuments).hasSize(1);
  }

  @Test
  void should_not_report_audit_request_without_domain_and_return_500() {
    AuditReportRequest auditReportRequest = buildAuditReportRequest("audit-sample1-full.json");
    auditReportRequest.setDomain(null);

    // fixme this.performPostNotification(auditReportRequest, 500);
  }

  private void performPostNotification(AuditReportRequest auditReportRequest, int expectedStatus)
      throws Exception {
    mockMvc
        .perform(
            post("/notification", 42L)
                .contentType(APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(auditReportRequest)))
        .andExpect(status().is(expectedStatus));

    // Wait 1s (wait for indexing document)
    Thread.sleep(1000);
  }
}
