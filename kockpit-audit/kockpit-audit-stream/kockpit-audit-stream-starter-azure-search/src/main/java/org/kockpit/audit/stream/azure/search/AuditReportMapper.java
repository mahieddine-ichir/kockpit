package org.kockpit.audit.stream.azure.search;

import org.kockpit.audit.stream.api.AuditReport;
import org.mapstruct.Mapper;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface AuditReportMapper {

    SearchAuditReport map(AuditReport auditReport);

    default Long fromInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.toEpochMilli();
    }

}
