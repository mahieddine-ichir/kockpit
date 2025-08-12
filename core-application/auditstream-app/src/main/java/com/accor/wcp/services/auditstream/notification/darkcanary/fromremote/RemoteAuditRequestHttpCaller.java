package com.accor.wcp.services.auditstream.notification.darkcanary.fromremote;

import com.accor.wcp.services.auditstream.notification.AuditReportHelper;
import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryConfiguration;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryEndpoint;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.ConfiguredAuditReportRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.accor.wcp.services.auditstream.notification.AuditReportHelper.*;
import static com.accor.wcp.services.auditstream.notification.darkcanary.DarkCanaryHelper.buildUrl;

@Component
@RequiredArgsConstructor
@Slf4j
public class RemoteAuditRequestHttpCaller {

    /**
     * TODO check/test template timeouts config - seems causing issues on Apigee calls
     * @see com.accor.wcp.services.auditstream.config.RestTemplateConfig
     */
    private final RestTemplate restTemplate = new RestTemplate();

    public List<RemoteCallMetadata> call(ConfiguredAuditReportRequest configuredAuditReportRequest) {
        AuditReportRequest auditReportRequest = configuredAuditReportRequest.auditReportRequest();
        DarkCanaryConfiguration darkCanaryConfiguration = configuredAuditReportRequest.darkCanaryConfiguration();

        return readRequestMethod(auditReportRequest)
                .map(String::toUpperCase)
                .map(HttpMethod::valueOf)
                .map(httpMethod -> readRequestUri(auditReportRequest)
                        .map(uri -> getEndpoints(uri, httpMethod, darkCanaryConfiguration))
                        .map(endpoints -> remoteCall(endpoints, configuredAuditReportRequest, httpMethod))
                        .orElse(Collections.emptyList())
                )
                .orElseGet(() -> {
                    log.warn("No request method found for {}", auditReportRequest);
                    return Collections.emptyList();
                });
    }

    private List<DarkCanaryEndpoint> getEndpoints(String uri, HttpMethod httpMethod, DarkCanaryConfiguration darkCanaryConfiguration) {
        return darkCanaryConfiguration.getEndpoints().stream()
                .filter(endpoint -> endpoint.getUri().equals(uri) &&
                        endpoint.getMethod().equalsIgnoreCase(httpMethod.name()))
                .toList();
    }

    private List<RemoteCallMetadata> remoteCall(List<DarkCanaryEndpoint> endpoints, ConfiguredAuditReportRequest configuredAuditReportRequest, HttpMethod method) {
        return endpoints.stream().map(endpoint -> this.remoteCall(endpoint, configuredAuditReportRequest, method)).collect(Collectors.toList());
    }

    private RemoteCallMetadata remoteCall(DarkCanaryEndpoint endpoint, ConfiguredAuditReportRequest configuredAuditReportRequest, HttpMethod method) {
        log.trace("Calling remote endpoint {} with method {}", endpoint, method);
        HttpHeaders headers = buildHttpHeaders(configuredAuditReportRequest.auditReportRequest());
        // replace any provided header
        if (! CollectionUtils.isEmpty(configuredAuditReportRequest.darkCanaryConfiguration().getHeaders())) {
            log.trace("Override with config headers for {}", configuredAuditReportRequest.darkCanaryConfiguration().getHeaders());
            configuredAuditReportRequest.darkCanaryConfiguration().getHeaders().forEach(header -> {
                if (CollectionUtils.isEmpty(header.getValues())) {
                    headers.remove(header.getKey());
                } else {
                    headers.put(header.getKey(), header.getValues());
                }
            });
        }
        log.info("Request headers {}", headers.entrySet());

        HttpEntity<String> entity;
        if (method == HttpMethod.POST) {
            entity = AuditReportHelper.readRequestBody(configuredAuditReportRequest.auditReportRequest())
                    .map(Object::toString)
                    .map(body -> new HttpEntity<>(body, headers))
                    .orElse(new HttpEntity<>(headers));
        } else {
            entity = new HttpEntity<>(headers);
        }
        String url = buildUrl(configuredAuditReportRequest.auditReportRequest(), endpoint);
        log.trace("Calling remote endpoint {} using url(params) {}", endpoint, url);
        Instant now = Instant.now();
        try {
            ResponseEntity<String> exchange = restTemplate.exchange(url, method, entity, String.class);
            long diffInMillis = Duration.between(now, Instant.now()).toMillis();
            return new RemoteCallMetadata(url, exchange.getBody(), method.name(), exchange.getStatusCode(), exchange.getBody(), diffInMillis, endpoint);
        } catch (RestClientException e) {
            log.error("Error calling remote {}, method {}, body {}", url, method.name(), entity.getBody(), e);
            return RemoteCallMetadata.error(url, method.name(), e.getMessage(), endpoint);
        }
    }
}
