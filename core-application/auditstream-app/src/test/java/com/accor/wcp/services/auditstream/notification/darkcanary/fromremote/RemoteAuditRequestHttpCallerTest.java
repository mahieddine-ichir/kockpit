package com.accor.wcp.services.auditstream.notification.darkcanary.fromremote;

import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryConfiguration;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryEndpoint;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.ConfiguredAuditReportRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(SpringExtension.class)
@Import(RemoteAuditRequestHttpCaller.class)
@Slf4j
class RemoteAuditRequestHttpCallerTest {

    @Autowired
    RemoteAuditRequestHttpCaller remoteAuditRequestHttpCaller;

    @MockitoBean
    RestTemplate restTemplate;

    @Test
    @DisplayName("On bestOffer call should call target endpoint with same parameters")
    void on_bestOfferCall_shouldCall_remote_withSame_queryParams() throws IOException {

        Mockito.when(restTemplate.exchange(anyString(), same(HttpMethod.GET), any(HttpEntity.class), same(String.class)))
                        .thenAnswer(invocationOnMock -> {
                            String url = invocationOnMock.getArgument(0, String.class);
                            assertEquals("http://localhost:8080?countryMarket=FR&dateIn=2025-12-01&lengthOfStay.unit=NIGHT&hotelIds=0911&adults=2&lengthOfStay.value=1&currency=EUR&childrenAges=1", url);
                            return ResponseEntity.ok("OK");
                        });

        ReflectionTestUtils.setField(remoteAuditRequestHttpCaller, "restTemplate", restTemplate);

        AuditReportRequest auditReportRequest = new ObjectMapper().registerModule(new JavaTimeModule())
                .readValue(getClass().getResourceAsStream("/darkcanary_testing/audit-sample-bestoffer.json"), AuditReportRequest.class);

        DarkCanaryEndpoint endpoint = new DarkCanaryEndpoint();
        endpoint.setTargetUri("http://localhost:8080");
        endpoint.setUri("/web/businessoffers/v1/availabilities/best-offers");
        endpoint.setEnvironment("dev");
        endpoint.setMethod("get");

        DarkCanaryConfiguration configuration = DarkCanaryConfiguration.builder()
                .strict(true)
                .appId("offers-business-api")
                .domain("wcoff")
                .endpoints(List.of(endpoint))
                .build();

        remoteAuditRequestHttpCaller.call(new ConfiguredAuditReportRequest(auditReportRequest, configuration));
    }

    @Test
    @DisplayName("On bestOffer call should call target endpoint with same parameters")
    void on_call_multiple_endpoints() throws IOException {
        Mockito.when(restTemplate.exchange(anyString(), same(HttpMethod.POST), any(HttpEntity.class), same(String.class)))
                .thenAnswer(invocationOnMock -> {
                    String url = invocationOnMock.getArgument(0, String.class);
                    assertEquals("https://dev-api.accor.com/businessoffers/v1/best-offers?countryMarket=FR&dateIn=2025-12-01&lengthOfStay.unit=NIGHT&hotelIds=0911&adults=2&lengthOfStay.value=1&currency=EUR&childrenAges=1", url);
                    return ResponseEntity.ok("OK");
                });

        ReflectionTestUtils.setField(remoteAuditRequestHttpCaller, "restTemplate", restTemplate);

        DarkCanaryConfiguration configuration = new ObjectMapper().readValue(this.getClass().getResourceAsStream("/darkcanary_testing/wcoff-businessoffers-api-manifest-int-darkcanary.json"), DarkCanaryConfiguration.class);
        Assertions.assertEquals(2, configuration.getEndpoints().size());

        AuditReportRequest auditReportRequest = new ObjectMapper().registerModule(new JavaTimeModule())
                .readValue(getClass().getResourceAsStream("/darkcanary_testing/audit-sample-bestoffer-post.json"), AuditReportRequest.class);

        remoteAuditRequestHttpCaller.call(new ConfiguredAuditReportRequest(auditReportRequest, configuration));
    }
}