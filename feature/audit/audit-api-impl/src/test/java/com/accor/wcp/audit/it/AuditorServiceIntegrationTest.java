package com.accor.wcp.audit.it;

import com.accor.wcp.audit.*;
import com.accor.wcp.audit.AuditReport.AuditJsonReport;
import com.accor.wcp.audit.obfuscate.FakeAuditEvent;
import com.accor.wcp.audit.obfuscate.HttpAuditedRequest;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest()
@ActiveProfiles({"integrationtest", "obfuscation1"})
class AuditorServiceIntegrationTest {

  @Autowired private AuditorService auditorService;

  @Autowired private AuditorKeyValueService auditorKeyValueService;

  @Autowired private AuditorEventService auditorEventService;

  @Autowired private LocalNotificationService localNotificationService;

  @Test
  void should_audit_simple_case() {
    // Start and stop :)
    auditorService.startAudit();
    auditorService.stopAuditAndNotify();

    // Get report
    List<AuditJsonReport> auditReport = localNotificationService.getAuditReport();
    assertThat(auditReport).isNotNull();
  }

  @Test
  void should_audit_and_obfuscate() throws URISyntaxException, IOException {
    // Start
    auditorService.startAudit();

    // Add index kv
    UserData userData1 = UserData.builder().age(25).name("Cyrilou Tib").build();
    auditorKeyValueService.addIndexedKeyValues(List.of(IndexedKeyValue.of("keyStirng", "value1")));
    auditorKeyValueService.addIndexedKeyValues(List.of(IndexedKeyValue.of("keyInt", 123456)));
    auditorKeyValueService.addIndexedKeyValues(List.of(IndexedKeyValue.of("keyFloat", 0.2255151f)));
    auditorKeyValueService.addIndexedKeyValues(List.of(IndexedKeyValue.of("keyDate", new Date())));
    auditorKeyValueService.addIndexedKeyValues(List.of(IndexedKeyValue.of("keyObject", userData1)));

    // Add events
    String type = "builtin.web";
    Map<String, List<String>> params = new HashMap<>();
    params.put("ids", List.of("777767890-0987654321-000", "234567890-0987654321-000"));
    params.put("emails", List.of("aaa@test.com", "aaa2@test.com"));
    FakeAuditEvent fakeAuditEvent1 = FakeAuditEvent.builder().build();
    String json = Files.readString(Path.of(getClass().getResource("/test-ob1.json").toURI()));
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.set("api-key", "1234567890-0987654321-000");
    fakeAuditEvent1.setRequest(
        HttpAuditedRequest.builder()
            .body(json)
            .uri("/bank/transaction/123456789")
            .headers(httpHeaders)
            .params(params)
            .build());
    fakeAuditEvent1.setHttpAuditedResponse(
        HttpAuditedRequest.builder().body(json).headers(httpHeaders).build());

    auditorEventService.addAuditEvents(type, List.of(fakeAuditEvent1));

    FakeAuditEvent fakeAuditEvent2 = FakeAuditEvent.builder().build();
    fakeAuditEvent2.setRequest(
        HttpAuditedRequest.builder()
            .body(json)
            .uri("/archive/transaction/123456789")
            .params(params)
            .headers(httpHeaders)
            .build());

    auditorEventService.addAuditEvents(type, List.of(fakeAuditEvent2));

    // Stop
    auditorService.stopAuditAndNotify();

    // Get report
    List<AuditJsonReport> auditReport = localNotificationService.getAuditReport();
    assertThat(auditReport).isNotNull();
    // Asserts
    AuditReport auditReport1 = auditReport.get(0).getAuditReport();
    assertThat(auditReport1).isNotNull();
    Audit audit1 = auditReport1.getAudits().get(0);
    FakeAuditEvent auditEvent1 = (FakeAuditEvent) audit1.getEvents().get(0);

    String obfuscatedContent1 =
            Files.readString(Path.of(getClass().getResource("/it/auditevent1-content.json").toURI()))
                    .trim();

    JSONAssert.assertEquals(auditEvent1.getRequest().getBody(),
            obfuscatedContent1,
            new DefaultComparator(JSONCompareMode.STRICT));

    assertThat(auditEvent1.getRequest().getHeaders().get("api-key"))
            .containsExactly("**********************000");
    assertThat(auditEvent1.getRequest().getParams().get("emails"))
            .containsExactly("a**@t*******", "a***@t*******");
    assertThat(auditEvent1.getRequest().getParams().get("ids"))
            .containsExactly("***********************0", "***********************0");
  }
}
