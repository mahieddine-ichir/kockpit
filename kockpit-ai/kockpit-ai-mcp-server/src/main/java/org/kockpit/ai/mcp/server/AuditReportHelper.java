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
import java.util.LinkedHashMap;
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
  public static List<Map<String, Object>> extractMockHttpExchanges(String originalJson) {
    AuditReport report = objectMapper.readValue(originalJson, AuditReport.class);
    if (report.getAudits() == null) {
      return List.of();
    }
    boolean domainHasMock = report.getDomain() != null
            && report.getDomain().toLowerCase().contains("mock");

    return report.getAudits().stream()
        .filter(audit -> "builtin.httpexchanges".equals(audit.get("type")))
        .filter(audit -> domainHasMock || requestPathContainsMock(audit))
        .map(audit -> {
          Map<String, Object> result = new LinkedHashMap<>();
          result.put("request", audit.get("request"));
          result.put("response", audit.get("response"));
          return result;
        })
        .toList();
  }

  @SuppressWarnings("unchecked")
  private static boolean requestPathContainsMock(Map<String, Object> exchange) {
    Object request = exchange.get("request");
    if (request instanceof Map<?, ?> requestMap) {
      Object uri = requestMap.get("uri");
      return uri instanceof String uriStr && uriStr.toLowerCase().contains("mock");
    }
    return false;
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
