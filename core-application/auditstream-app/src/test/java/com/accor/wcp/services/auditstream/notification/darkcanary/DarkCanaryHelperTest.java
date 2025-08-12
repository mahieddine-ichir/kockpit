package com.accor.wcp.services.auditstream.notification.darkcanary;

import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryEndpoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.accor.wcp.services.auditstream.notification.darkcanary.DarkCanaryHelper.buildUrl;

class DarkCanaryHelperTest {

    @Test
    @SneakyThrows
    void buildUrlWithParams() {
        AuditReportRequest auditReportRequest = new ObjectMapper().registerModule(new JavaTimeModule())
                .readValue(getClass().getResourceAsStream("/darkcanary_testing/audit-sample.json"), AuditReportRequest.class);

        DarkCanaryEndpoint endpoint = new DarkCanaryEndpoint();
        endpoint.setTargetUri("http://localhost:8080");
        String url = buildUrl(auditReportRequest, endpoint);

        Assertions.assertEquals("http://localhost:8080?id=1234&name=joe", url);
    }

    @Test
    @SneakyThrows
    void buildUrlWithParams_withEncoding() {
        AuditReportRequest auditReportRequest = new ObjectMapper().registerModule(new JavaTimeModule())
                .readValue(getClass().getResourceAsStream("/darkcanary_testing/audit-sample-bestoffer.json"), AuditReportRequest.class);

        DarkCanaryEndpoint endpoint = new DarkCanaryEndpoint();
        endpoint.setTargetUri("http://localhost:8080");
        String url = buildUrl(auditReportRequest, endpoint);

        Assertions.assertEquals("http://localhost:8080?countryMarket=FR&dateIn=2025-12-01&lengthOfStay.unit=NIGHT&hotelIds=0911&adults=2&lengthOfStay.value=1&currency=EUR&childrenAges=1", url);
    }

    @Test
    @SneakyThrows
    void buildUrlWithoutParams() {
        DarkCanaryEndpoint endpoint = new DarkCanaryEndpoint();
        endpoint.setTargetUri("http://localhost:8080");
        String url = buildUrl(new AuditReportRequest(), endpoint);

        Assertions.assertEquals("http://localhost:8080", url);
    }

}