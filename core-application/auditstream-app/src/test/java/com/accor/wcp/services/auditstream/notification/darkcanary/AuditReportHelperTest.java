package com.accor.wcp.services.auditstream.notification.darkcanary;

import com.accor.wcp.services.auditstream.notification.AuditReportHelper;
import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.Map;

class AuditReportHelperTest {

    @Test
    void read_traceId() throws IOException {
        AuditReportRequest auditReportRequest = new ObjectMapper().registerModule(new JavaTimeModule())
                .readValue(getClass().getResourceAsStream("/darkcanary_testing/audit-sample.json"), AuditReportRequest.class);

        Assertions.assertEquals("3e1f4faa40a69667e9f50357c195454b", AuditReportHelper.readTraceId(auditReportRequest));
    }

    @Test
    void find_duration() throws IOException {
        AuditReportRequest auditReportRequest = new ObjectMapper().registerModule(new JavaTimeModule())
                .readValue(getClass().getResourceAsStream("/darkcanary_testing/audit-sample.json"), AuditReportRequest.class);

        Assertions.assertEquals(407, AuditReportHelper.readCallDuration(auditReportRequest).get());
    }

    @SneakyThrows
    @Test
    @DisplayName("Should read a header, ignoring case")
    void on_flux_should_read_headers() {
        AuditReportRequest auditReportRequest = new ObjectMapper().registerModule(new JavaTimeModule())
                .readValue(getClass().getResourceAsStream("/darkcanary_testing/audit-sample.json"), AuditReportRequest.class);

        Assertions.assertEquals("3e1f4faa40a69667e9f50357c195454b", AuditReportHelper.readHeaderValue(auditReportRequest, "x-b3-traceid").get());
        Assertions.assertEquals("3e1f4faa40a69667e9f50357c195454b", AuditReportHelper.readHeaderValue(auditReportRequest, "X-B3-TRACEID").get());
        Assertions.assertEquals("3e1f4faa40a69667e9f50357c195454b", AuditReportHelper.readHeaderValue(auditReportRequest, "X-B3-TraceId").get());
    }

    @SneakyThrows
    @Test
    @DisplayName("Should read the http response")
    void on_flux_should_read_response_when_it_exists() {
        AuditReportRequest auditReportRequest = new ObjectMapper().registerModule(new JavaTimeModule())
                .readValue(getClass().getResourceAsStream("/darkcanary_testing/audit-sample.json"), AuditReportRequest.class);

        Assertions.assertEquals("Hello Cyril !", AuditReportHelper.readResponse(auditReportRequest).get());
    }

    @SneakyThrows
    @Test
    @DisplayName("Should read a uri")
    void should_read_uri_when_it_exists() {
        AuditReportRequest auditReportRequest = new ObjectMapper().registerModule(new JavaTimeModule())
                .readValue(getClass().getResourceAsStream("/darkcanary_testing/audit-sample.json"), AuditReportRequest.class);

        Assertions.assertEquals("/wcpsamples/sayHello/Cyril", AuditReportHelper.readRequestUri(auditReportRequest).get());
    }

    @SneakyThrows
    @Test
    @DisplayName("Should read request param's")
    void should_read_request_params_when_it_exists() {
        AuditReportRequest auditReportRequest = new ObjectMapper().registerModule(new JavaTimeModule())
                .readValue(getClass().getResourceAsStream("/darkcanary_testing/audit-sample.json"), AuditReportRequest.class);

        Assertions.assertEquals(Map.of("id", 1234, "name", "joe"), AuditReportHelper.readRequestParameters(auditReportRequest));
    }

    @Test
    @SneakyThrows
    void buildHttpHeaders() {
        AuditReportRequest auditReportRequest = new ObjectMapper().registerModule(new JavaTimeModule())
                .readValue(getClass().getResourceAsStream("/darkcanary_testing/audit-sample.json"), AuditReportRequest.class);

        HttpHeaders headers = AuditReportHelper.buildHttpHeaders(auditReportRequest);
        Assertions.assertTrue(headers.containsKey("X-B3-TraceId"));
    }

    @Test
    @SneakyThrows
    void getBody() {
        AuditReportRequest auditReportRequest = new ObjectMapper().registerModule(new JavaTimeModule())
                .readValue(getClass().getResourceAsStream("/darkcanary_testing/download_report_offers-business-api.json"), AuditReportRequest.class);

        AuditReportHelper.readRequestBody(auditReportRequest)
                .ifPresentOrElse(System.out::println, Assertions::fail);

    }
}