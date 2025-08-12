package com.accor.wcp.services.auditstream.notification.es.manager;

import com.accor.wcp.console.services.audit.kengine.KEngineRegistryWriteRepository;
import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.AuditReportRequestConsumer;
import com.accor.wcp.services.auditstream.notification.es.AuditIndexEsRepositoryV2;
import com.accor.wcp.services.auditstream.notification.es.AuditReportDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Maps;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;

import static com.accor.wcp.services.auditstream.notification.service.AuditReportService.computeTtl;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditReportRequestConsumerForAudit implements AuditReportRequestConsumer {

    private final KEngineRegistryWriteRepository registryRepository;

    private final ObjectMapper objectMapper;

    private final AuditIndexEsRepositoryV2 auditIndexEsRepositoryV2;

    @PostConstruct
    void init() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void accept(List<AuditReportRequest> auditReportRequests) {
        // Create index requests
        List<AuditIndexRequest> indexRequests = computeIndexRequests(auditReportRequests);

        // Bulk save
        try {
            auditIndexEsRepositoryV2.bulkIndexRequests(indexRequests);
        } catch (IOException e) {
            log.warn("Error bulk indexing requests: {}. Error message: {}", indexRequests, e.getMessage(), e);
        }
    }

    private List<AuditIndexRequest> computeIndexRequests(List<AuditReportRequest> auditReportRequests) {
        return auditReportRequests.stream()
                .map(this::computeIndexRequest)
                .filter(Objects::nonNull)
                .toList();
    }

    private AuditIndexRequest computeIndexRequest(AuditReportRequest auditReportRequest) {
        // Validate
        String domain = auditReportRequest.getDomain();
        String env = auditReportRequest.getEnv();
        if (Objects.isNull(domain) || Objects.isNull(env)) {
            log.warn(
                    "Missing domain or env, unable to determine index name, input data: {}",
                    auditReportRequest);
            return null;
        }

        // Optimize auditReport storage
        auditReportRequest = optimizeForStorage(auditReportRequest);

        String auditReportDocument;
        try {
            auditReportDocument = constructEsDocumentToIndex(auditReportRequest);
        } catch (IOException e) {
            log.warn("Error constructing ES Document: {}", e.getMessage());
            return null;
        }

        // Manage TTL by msg
        int ttl = computeTtl(auditReportRequest);

        return AuditIndexRequest.builder()
                .docId(auditReportRequest.getRequestId())
                .domain(domain)
                .env(env)
                .ttl(ttl)
                .auditReportJson(auditReportDocument)
                .build();
    }

    private String constructEsDocumentToIndex(AuditReportRequest auditReportRequest)
            throws IOException {

        // Original json request use to map auditReportRequest and store compressed version to Es
        // document
        String originalJsonSource = objectMapper.writeValueAsString(auditReportRequest);
        String compressedOriginalJsonSource = compressOriginalJsonSource(originalJsonSource);

        // We keep only indexedKeyValues key with existing values from all extensions (used for search
        // by key)
        List<Map<String, Object>> indexedKeyValues = new ArrayList<>();
        if (nonNull(auditReportRequest.getExtensions())) {
            indexedKeyValues =
                    auditReportRequest.getExtensions().stream()
                            .filter(Objects::nonNull)
                            .map(stringObjectMap -> Maps.filterKeys(stringObjectMap, "indexedKeyValues"::equals))
                            .map(filterEmptyListOfKeyValues())
                            .filter(stringObjectMap -> isNotEmpty(stringObjectMap.values()))
                            .toList();
        } else if (nonNull(auditReportRequest.getAudits())) {
            indexedKeyValues =
                    List.of(Map.of("indexedKeyValues", auditReportRequest.getIndexedKeyValues()));
        }

        // Create Es document
        AuditReportDocument auditReportDocument =
                objectMapper.readValue(originalJsonSource, AuditReportDocument.class);
        auditReportDocument.setCompressedOriginalJsonValue(compressedOriginalJsonSource);
        auditReportDocument.setIndexedExtensions(indexedKeyValues);
        // Add timestamp field (mandatory for rollover)
        auditReportDocument.setTimestamp(Instant.now().toEpochMilli());

        return objectMapper.writeValueAsString(auditReportDocument);
    }

    /**
     * @deprecated Optimization for storage should be removed when all consumers will use WCP >= 2.6.1
     */
    @Deprecated
    protected AuditReportRequest optimizeForStorage(AuditReportRequest auditReportRequest) {
        String domain = auditReportRequest.getDomain();
        String env = auditReportRequest.getEnv();
        String applicationId = auditReportRequest.getAppId();

        if (nonNull(auditReportRequest.getAudits())) {
            auditReportRequest.getAudits().stream()
                    .filter(audit -> nonNull(audit.get("type")) && "kengine.flows".equals(audit.get("type")))
                    .flatMap(audit -> ((List<Map<String, Object>>) audit.get("events")).stream())
                    .forEach(event -> extractReferentialFromAuditReport(domain, env, applicationId, event));
            return auditReportRequest;
        }

        deprecatedOptimizeForStorage(auditReportRequest, domain, env, applicationId);

        return auditReportRequest;
    }


    @Deprecated
    private AuditReportRequest deprecatedOptimizeForStorage(
            AuditReportRequest auditReportRequest, String domain, String env, String applicationId) {
        if (isNull(auditReportRequest.getExtensions())) {
            return auditReportRequest;
        }

        // Special case for flow extension (kengine.flows)
        // Extract executionEDTDTO.referential
        auditReportRequest.getExtensions().stream()
                .filter(audit -> nonNull(audit.get("type")) && "kengine.flows".equals(audit.get("type")))
                .forEach(audit -> extractReferentialFromAuditReport(domain, env, applicationId, audit));

        return auditReportRequest;
    }

    @Deprecated
    private void extractReferentialFromAuditReport(
            String domain, String env, String applicationId, Map<String, Object> stringObjectMap) {
        Map<String, Object> executionEDTDTO = (Map<String, Object>) stringObjectMap.get("executionEDTDTO");

        // Last process (fullRegistryReferentialId)
        Object fullRegistryReferentialId = executionEDTDTO.get("fullRegistryReferentialId");
        if (nonNull(fullRegistryReferentialId)) {
            return;
        }

        // @Deprecated
        deprecatedExtractReferentialFromAuditReport(domain, env, applicationId, executionEDTDTO);
    }

    @Deprecated
    private void deprecatedExtractReferentialFromAuditReport(
            String domain, String env, String applicationId, Map<String, Object> executionEDTDTO) {
        Map<String, Object> referential = (Map<String, Object>) executionEDTDTO.get("referential");
        if (isNull(referential)) {
            return;
        }
        String referentialId =
                registryRepository.getReferentialId(domain, env, applicationId, referential);
        executionEDTDTO.remove("referential");
        executionEDTDTO.put("ruleReferentialId", referentialId);
    }

    private Function<Map<String, Object>, Map<String, Object>> filterEmptyListOfKeyValues() {
        return stringObjectMap ->
                Maps.filterValues(
                        stringObjectMap, value -> value instanceof List list && isNotEmpty((list)));
    }

    private String compressOriginalJsonSource(String originalJsonSource) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(originalJsonSource.length())) {

            try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
                gzip.write(originalJsonSource.getBytes());
            }
            byte[] compressed = bos.toByteArray();
            return Base64.encodeBase64String(compressed);
        }
    }

}
