package org.kockpit.ai.mcp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.kockpit.ai.mcp.server.dto.AuditReport;
import org.kockpit.ai.mcp.server.dto.AuditReportDocument;
import org.mapstruct.factory.Mappers;
import org.opensearch.search.SearchHit;

public class AuditReportHelper {

  private final static ObjectMapper objectMapper = new ObjectMapper()
          .registerModule(new JavaTimeModule());


  @SneakyThrows
  public static  AuditReport convertForViewList(SearchHit hit) {
    AuditReportDocument document = convertFromString(hit.getSourceAsString());
    return Mappers.getMapper(AuditReportMapper.class).mapAuditReport(document);
  }


  @SneakyThrows
  public static  AuditReportDocument convertFromString(String sourceAsString) {
    return objectMapper.readValue(sourceAsString, AuditReportDocument.class);
  }
}
