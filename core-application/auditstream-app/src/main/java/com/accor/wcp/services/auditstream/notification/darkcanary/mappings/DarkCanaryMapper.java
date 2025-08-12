package com.accor.wcp.services.auditstream.notification.darkcanary.mappings;

import com.accor.wcp.services.auditstream.notification.AuditReportHelper;
import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.DarkCanaryIndexDocument;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.DarkCanaryIndexDocumentByDifference;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.Endpoint;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.ArrayList;

@Mapper(componentModel = "spring")
public interface DarkCanaryMapper {

    @Mapping(target = "timestamp", expression = "java(timestamp())")
    @Mapping(target = "responseLeft", expression = "java(responseV1(auditReportRequest))")
    @Mapping(target = "traceId", expression = "java(traceId(auditReportRequest))")
    DarkCanaryIndexDocument toDarkCanaryIndexDocument(AuditReportRequest auditReportRequest);

    @Mapping(target = "difference", ignore = true)
    DarkCanaryIndexDocumentByDifference toDarkCanaryIndexDocumentByDifference(DarkCanaryIndexDocument darkCanaryIndexDocument);

    default Integer ttl(AuditReportRequest auditReportRequest) {
        return AuditReportHelper.computeTtl(auditReportRequest);
    }

    default String traceId(AuditReportRequest auditReportRequest) {
        return AuditReportHelper.readTraceId(auditReportRequest);
    }

    default Long timestamp() {
        return Instant.now().toEpochMilli();
    }

    @AfterMapping
    default void endpoint(@MappingTarget DarkCanaryIndexDocument darkCanaryIndexDocument, AuditReportRequest auditReportRequest) {
        AuditReportHelper.readRequestUri(auditReportRequest)
                .ifPresent(uri -> {
                    Endpoint endpoint = new Endpoint();
                    endpoint.setEnv(auditReportRequest.getEnv());
                    endpoint.setUri(uri);

                    if (CollectionUtils.isEmpty(darkCanaryIndexDocument.getEndpoints())) {
                        darkCanaryIndexDocument.setEndpoints(new ArrayList<>());
                    }
                    darkCanaryIndexDocument.getEndpoints().add(endpoint);
                });
    }

    default String responseV1(AuditReportRequest auditReportRequest) {
        return AuditReportHelper.readResponse(auditReportRequest)
                .orElse(null);
    }
}
