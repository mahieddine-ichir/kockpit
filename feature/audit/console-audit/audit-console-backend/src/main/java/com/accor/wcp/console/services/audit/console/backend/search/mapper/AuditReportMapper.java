package com.accor.wcp.console.services.audit.console.backend.search.mapper;

import static java.util.Objects.nonNull;

import com.accor.wcp.console.services.audit.console.backend.search.dto.AuditReport;
import com.accor.wcp.console.services.audit.console.backend.search.dto.AuditReportDocument;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper
public interface AuditReportMapper {
  @Mapping(
      target = "indexedKeyValues",
      source = "indexedExtensions",
      qualifiedByName = "mapKeyValues")
  AuditReport mapAuditReport(AuditReportDocument document);

  @Named("mapKeyValues")
  default List<Map<String, Object>> mapKeyValues(List<Map<String, Object>> indexedFields) {
    return indexedFields.stream()
        .filter(e -> nonNull(e.get("indexedKeyValues")))
        .flatMap(e -> ((List<Map<String, Object>>) e.get("indexedKeyValues")).stream())
        .toList();
  }
}
