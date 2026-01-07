package org.kockpit.ai.mcp.server;

import org.kockpit.ai.mcp.server.dto.AuditReport;
import org.kockpit.ai.mcp.server.dto.AuditReportDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

@Mapper
public interface AuditReportMapper {

  @Mapping(target = "indexedKeyValues", source = "indexedExtensions", qualifiedByName = "mapKeyValues")
  AuditReport mapAuditReport(AuditReportDocument document);

  @Named("mapKeyValues")
  default List<Map<String, Object>> mapKeyValues(List<Map<String, Object>> indexedFields) {
    return indexedFields.stream()
        .filter(e -> nonNull(e.get("indexedKeyValues")))
        .flatMap(e -> ((List<Map<String, Object>>) e.get("indexedKeyValues")).stream())
        .toList();
  }
}
