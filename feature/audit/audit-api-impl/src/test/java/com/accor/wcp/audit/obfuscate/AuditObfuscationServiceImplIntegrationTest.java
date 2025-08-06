package com.accor.wcp.audit.obfuscate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.accor.wcp.audit.AuditImpl;
import com.accor.wcp.audit.AuditReport;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest()
@ActiveProfiles("obfuscation1")
class AuditObfuscationServiceImplIntegrationTest {

  @Autowired private AuditObfuscationServiceImpl auditObfuscationServiceImpl;

  @Test
  void should_obfuscate() throws URISyntaxException, IOException {
    AuditReport auditReport = createAuditReport();

    assertThat(auditObfuscationServiceImpl).isNotNull();

    auditObfuscationServiceImpl.obfuscate(auditReport);

    // Assert obfuscation
    // TODO assert auditReport
    System.out.println(auditObfuscationServiceImpl);
  }

  private AuditReport createAuditReport() throws IOException, URISyntaxException {
    AuditReport report = AuditReport.builder().build();

    FakeAuditEvent fakeAuditEvent1 = FakeAuditEvent.builder().build();
    String json =
        Files.readString(
            Path.of(AuditObfuscationServiceImplTest.class.getResource("/test-ob1.json").toURI()));
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.set("api-key", "1234567890-0987654321-000");
    fakeAuditEvent1.setRequest(
        HttpAuditedRequest.builder()
            .body(json)
            .uri("/bank/transaction/123456789")
            .headers(httpHeaders)
            .build());
    fakeAuditEvent1.setHttpAuditedResponse(
        HttpAuditedRequest.builder().body(json).headers(httpHeaders).build());

    FakeAuditEvent fakeAuditEvent2 = FakeAuditEvent.builder().build();
    fakeAuditEvent2.setRequest(
        HttpAuditedRequest.builder()
            .body(json)
            .uri("/archive/transaction/123456789")
            .headers(httpHeaders)
            .build());

    String typeWeb = "builtin.web";
    AuditImpl audit =
        AuditImpl.builder().type(typeWeb).events(List.of(fakeAuditEvent1, fakeAuditEvent2)).build();
    report.auditsMap().put(typeWeb, audit);

    return report;
  }
}
