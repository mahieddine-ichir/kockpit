package org.kockpit.audit.stream.opensearch;

import org.kockpit.audit.stream.api.model.AuditReport;
import org.kockpit.audit.stream.api.model.IndexedKeyValue;
import org.kockpit.audit.stream.opensearch.model.SearchAuditReport;
import org.kockpit.audit.stream.opensearch.model.SearchIndexedKeyValue;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface AuditReportMapper {

    SearchAuditReport map(AuditReport auditReport);

    @AfterMapping
    default void addTimestamps(@MappingTarget SearchAuditReport auditReport) {
        auditReport.setTimestamp(Instant.now().toEpochMilli());
    }

    SearchIndexedKeyValue map(IndexedKeyValue indexedKeyValue);

    default Long fromInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.toEpochMilli();
    }
}
