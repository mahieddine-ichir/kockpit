package org.kockpit.ai.mcp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.kockpit.ai.mcp.server.dto.AuditReport;
import org.kockpit.ai.mcp.server.dto.AuditReportDocument;
import org.mapstruct.factory.Mappers;
import org.opensearch.search.SearchHit;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public abstract class AuditReportHelper {

  private final static ObjectMapper objectMapper = new ObjectMapper()
          .registerModule(new JavaTimeModule());


  @SneakyThrows
  static AuditReport convertForViewList(SearchHit hit) {
    AuditReportDocument document = convertFromString(hit.getSourceAsString());
    return Mappers.getMapper(AuditReportMapper.class).mapAuditReport(document);
  }

  @SneakyThrows
  public static AuditReportDocument convertFromString(String sourceAsString) {
    return objectMapper.readValue(sourceAsString, AuditReportDocument.class);
  }

  @SneakyThrows
  public static String decompressOriginalJson(String compressedOriginalJsonValue) {
    byte[] compressed = Base64.getDecoder().decode(compressedOriginalJsonValue);
    try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
      return new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  public static List<Map<String, Object>> extractKengineFlowsAudits(String originalJson) {
    return extractAuditsByType(originalJson, "kengine.flows");
  }

  public static List<Map<String, Object>> extractBuiltinHttpExchangesAudits(String originalJson) {
    return extractAuditsByType(originalJson, "builtin.httpexchanges");
  }

  @SneakyThrows
  public static List<Map<String, Object>> extractAuditsByType(String originalJson, String type) {
    AuditReport report = objectMapper.readValue(originalJson, AuditReport.class);
    if (report.getAudits() == null) {
      return List.of();
    }
    return report.getAudits().stream()
        .filter(audit -> type.equals(audit.get("type")))
        .toList();
  }
}
