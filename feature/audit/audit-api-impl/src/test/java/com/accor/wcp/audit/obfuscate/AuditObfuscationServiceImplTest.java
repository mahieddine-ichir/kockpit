package com.accor.wcp.audit.obfuscate;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.accor.wcp.audit.AuditImpl;
import com.accor.wcp.audit.AuditReport;
import com.accor.wcp.obfuscation.ObfuscationService;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class AuditObfuscationServiceImplTest {

  @Test
  void should_obfuscate() throws URISyntaxException, IOException {
    // Given
    String type1 = "type1"; // could be "builtin.web"
    AuditObfuscationSettings global = new AuditObfuscationSettings();
    ModuleObfuscationSettings type1ObfuscationSettings1 = new ModuleObfuscationSettings();
    type1ObfuscationSettings1.setId(type1);
    type1ObfuscationSettings1
        .getFilters()
        .add(new ObfuscationFilter("request.uri", "\\/bank\\/transaction\\/(.*)"));
    type1ObfuscationSettings1
        .getProperties()
        .put(
            "request.body",
            AuditPropertyObfuscationSettings.builder()
                .payloadPaths(
                    List.of(
                        AuditPathConfig.builder()
                            .path("$.items[*].mainBeneficiary.phone.number")
                            .mask("phone")
                            .build(),
                        AuditPathConfig.builder()
                            .path("$.items[*].mainBeneficiary.firstName")
                            .mask("name")
                            .build(),
                        AuditPathConfig.builder()
                            .path("$.items[*].mainBeneficiary.lastName")
                            .build(),
                        AuditPathConfig.builder()
                            .path("$.items[*].mainBeneficiary.email")
                            .mask("email")
                            .build()))
                .build());

    //    ModuleObfuscationSettings type1ObfuscationSettings2 = new ModuleObfuscationSettings();
    //    type1ObfuscationSettings2.setFilterPath("request.uri");
    //    type1ObfuscationSettings2.setFilterExpression("\\/archive\\/transaction\\/(.*)");
    //    ObfuscationConfigDefault config2 =
    //        ObfuscationConfigDefault.builder()
    //            .bodyPathConfigs(
    //                List.of(
    //
    // PathConfigDefault.builder().path("$.items[*].mainBeneficiary.lastName").build(),
    //                    PathConfigDefault.builder()
    //                        .path("$.items[*].mainBeneficiary.email")
    //                        .mask("email")
    //                        .build(),
    //                    PathConfigDefault.builder().path("$..offerId").mask("apiKey").build(),
    //
    // PathConfigDefault.builder().path("$.items[*].comment").mask("apiKey").build()))
    //            .build();
    //    type1ObfuscationSettings2.setDefaultSettings(config2);

    global.getConfigs().add(type1ObfuscationSettings1);

    ObfuscationService obfuscationService = Mockito.mock(ObfuscationService.class);
    AuditObfuscationServiceImpl underTest =
        new AuditObfuscationServiceImpl(obfuscationService, global);

    AuditReport report = AuditReport.builder().build();

    FakeAuditEvent fakeAuditEvent1 = FakeAuditEvent.builder().build();
    String json =
        Files.readString(
            Path.of(AuditObfuscationServiceImplTest.class.getResource("/test-ob1.json").toURI()));
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    fakeAuditEvent1.setRequest(
        HttpAuditedRequest.builder()
            .body(json)
            .uri("/bank/transaction/123456789")
            .headers(httpHeaders)
            .build());

    FakeAuditEvent fakeAuditEvent2 = FakeAuditEvent.builder().build();
    fakeAuditEvent2.setRequest(
        HttpAuditedRequest.builder()
            .body(json)
            .uri("/archive/transaction/123456789")
            .headers(httpHeaders)
            .build());

    AuditImpl audit =
        AuditImpl.builder().type(type1).events(List.of(fakeAuditEvent1, fakeAuditEvent2)).build();
    report.auditsMap().put(type1, audit);

    // When
    underTest.obfuscate(report);

    // Then
    // Obfuscated config 1
    assertThat(fakeAuditEvent1.getRequest().getBody()).isNotNull();
    System.out.println("Obfuscated body 1: " + fakeAuditEvent1.getRequest().getBody());
    // Obfuscated config 2
    System.out.println("Obfuscated body 2: " + fakeAuditEvent2.getRequest().getBody());
  }
}
