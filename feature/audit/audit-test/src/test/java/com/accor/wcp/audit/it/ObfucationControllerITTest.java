package com.accor.wcp.audit.it;

import com.accor.wcp.audit.AuditReport.AuditJsonReport;
import com.accor.wcp.audit.module.web.WebAuditEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SpringBoot4IntegrationTestApplication.class)
@ActiveProfiles({"integrationtest", "obfuscation"})
@AutoConfigureMockMvc
class ObfucationControllerITTest {

  @Autowired private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    AuditReportCache.clearCache();
  }

  @Test
  void should_obfuscate_web_builtin_json_web() throws Exception {
    // GIVEN
    // WHEN
    this.mockMvc
        .perform(
            MockMvcRequestBuilders.post("/audit/obfuscate/web/json")
                .content(
                    Files.readString(
                        Paths.get("src/test/resources/data/web-json-to-obfuscate.json")))
                .headers(getHttpHeaders(MediaType.APPLICATION_JSON))
                .accept(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk());

    AuditJsonReport auditJsonReport = AuditReportCache.getReport();

    // THEN
    assertThat(auditJsonReport).isNotNull();
    assertThat(auditJsonReport.getAuditReport().getAudits()).hasSize(1);
    assertThat(auditJsonReport.getAuditReport().getAudits().get(0).getEvents()).hasSize(1);
    assertThat(auditJsonReport.getAuditReport().getAudits().get(0).getEvents().get(0))
        .isInstanceOf(WebAuditEvent.class);
    WebAuditEvent audit =
        (WebAuditEvent) auditJsonReport.getAuditReport().getAudits().get(0).getEvents().get(0);
    assertThat(audit.getHttpAuditedRequest().getHeaders().get("api-key"))
            .containsExactly("*************************sf");

    assertEquals(
        audit.getHttpAuditedRequest().getBody(),
        Files.readString(
            Paths.get("src/test/resources/data/obfuscated/web-json-obfuscated-request.json")),
        new DefaultComparator(JSONCompareMode.STRICT));
    assertEquals(
        audit.getHttpAuditedResponse().getBody(),
        Files.readString(
            Paths.get("src/test/resources/data/obfuscated/web-json-obfuscated-response.json")),
        new DefaultComparator(JSONCompareMode.STRICT));
  }

  private HttpHeaders getHttpHeaders(MediaType mediaType) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(mediaType);
    headers.set("api-key", "89-88s79878s7fkjkskfslkflsf");
    return headers;
  }

  @Test
  void should_obfuscate_web_builtin_xml_web() throws Exception {
    // GIVEN
    // WHEN
    this.mockMvc
        .perform(
            MockMvcRequestBuilders.post("/audit/obfuscate/web/xml")
                .content(
                    Files.readString(Paths.get("src/test/resources/data/web-xml-to-obfuscate.xml")))
                .headers(getHttpHeaders(MediaType.APPLICATION_XML))
                .accept(MediaType.APPLICATION_XML))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk());

    // THEN
    AuditJsonReport auditJsonReport = AuditReportCache.getReport();

    // THEN
    assertThat(auditJsonReport).isNotNull();
    assertThat(auditJsonReport.getAuditReport().getAudits()).hasSize(1);
    assertThat(auditJsonReport.getAuditReport().getAudits().get(0).getEvents()).hasSize(1);
    assertThat(auditJsonReport.getAuditReport().getAudits().get(0).getEvents().get(0))
        .isInstanceOf(WebAuditEvent.class);
    WebAuditEvent audit =
        (WebAuditEvent) auditJsonReport.getAuditReport().getAudits().get(0).getEvents().get(0);
    assertThat(audit.getHttpAuditedRequest().getHeaders().get("api-key"))
            .containsExactly("************************lsf");

    assertThat(audit.getHttpAuditedRequest().getBody())
        .isEqualToIgnoringNewLines(
            Files.readString(
                Paths.get("src/test/resources/data/obfuscated/web-xml-request-obfuscated.xml")));
    assertThat(audit.getHttpAuditedResponse().getBody())
        .isEqualToIgnoringNewLines(
            Files.readString(
                Paths.get("src/test/resources/data/obfuscated/web-xml-response-obfuscated.xml")));
  }
}
