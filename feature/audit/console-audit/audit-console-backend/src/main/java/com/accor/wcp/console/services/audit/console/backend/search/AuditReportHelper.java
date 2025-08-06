package com.accor.wcp.console.services.audit.console.backend.search;

import static java.util.Objects.nonNull;

import com.accor.wcp.console.services.audit.console.backend.search.dto.AuditReport;
import com.accor.wcp.console.services.audit.console.backend.search.dto.AuditReportDocument;
import com.accor.wcp.console.services.audit.console.backend.search.dto.SearchType;
import com.accor.wcp.console.services.audit.console.backend.search.mapper.AuditReportMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.codec.binary.Base64;
import org.mapstruct.factory.Mappers;
import org.opensearch.search.SearchHit;

@UtilityClass
public class AuditReportHelper {

  public static final String SUB_TYPE_BUILTIN_KV = "builtin.kv";

  public static final String FIELD_APP_ID_V2 = "appId";
  public static final String FIELD_KV_NESTED_KEY = "indexedExtensions.indexedKeyValues.key";
  public static final String FIELD_KV_NESTED_VALUE_STRING =
      "indexedExtensions.indexedKeyValues.value";
  public static final String FIELD_KV_NESTED_VALUE_INTEGER =
      "indexedExtensions.indexedKeyValues.valueInteger";
  public static final String FIELD_KV_NESTED_VALUE_FLOAT =
      "indexedExtensions.indexedKeyValues.valueFloat";
  public static final String FIELD_KV_NESTED_VALUE_DATE =
      "indexedExtensions.indexedKeyValues.valueDate";
  public static final String FIELD_KV_NESTED_PATH = "indexedExtensions.indexedKeyValues";
  public static final String FIELD_ID = "_id";
  public static final String FIELD_START = "start";

  private final ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  public String getAuditAliasName(String domain, String indexName, String env) {
    String indexFullName = domain + "-" + indexName + "-" + env + "-read";
    return indexFullName.toLowerCase();
  }

  public String getOriginalJsonValue(AuditReportDocument auditReport) throws IOException {
    // Deprecated case (old one => not compressed)
    String originalJsonValue = auditReport.getOriginalJsonValue();
    if (nonNull(originalJsonValue)) {
      return originalJsonValue;
    }

    // Decompress
    String compressedOriginalJsonValue = auditReport.getCompressedOriginalJsonValue();
    return decompress(compressedOriginalJsonValue);
  }

  public String decompress(String base64Compressed) throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(base64Compressed));
    GZIPInputStream gis = new GZIPInputStream(bis);
    BufferedReader br = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) {
      sb.append(line);
    }
    br.close();
    gis.close();
    bis.close();
    return sb.toString();
  }

  @SneakyThrows
  public AuditReport convertForViewList(SearchHit hit) {
    AuditReportDocument document = convertFromString(hit.getSourceAsString());
    return Mappers.getMapper(AuditReportMapper.class).mapAuditReport(document);
  }

  @SneakyThrows
  public AuditReport convertForViewDetail(SearchHit hit) {
    AuditReportDocument document = convertFromString(hit.getSourceAsString());
    AuditReport auditReport = Mappers.getMapper(AuditReportMapper.class).mapAuditReport(document);

    String originalJsonValue = AuditReportHelper.getOriginalJsonValue(document);
    AuditReport originalAuditReport = objectMapper.readValue(originalJsonValue, AuditReport.class);

    auditReport.setAudits(originalAuditReport.getAudits());

    // Old way = convert extensions to new audit model
    deprecatedConvertForViewDetail(auditReport, originalAuditReport);

    return auditReport;
  }

  @Deprecated(forRemoval = true, since = "2.6.1")
  private static void deprecatedConvertForViewDetail(
      AuditReport auditReport, AuditReport originalAuditReport) {
    List<Map<String, Object>> extensions = originalAuditReport.getExtensions();
    if (Objects.nonNull(extensions)) {
      auditReport.setAudits(convertExtensionsToAuditEvents(extensions));
    }
  }

  @Deprecated(forRemoval = true, since = "2.6.1")
  private List<Map<String, Object>> convertExtensionsToAuditEvents(
      List<Map<String, Object>> extensions) {
    List<Map<String, Object>> audits = new ArrayList<>();

    extensions.stream()
        .filter(ext -> !"builtin.kv".equals(ext.get("type")))
        .forEach(
            ext -> {
              String type = (String) ext.get("type");
              Optional<Map<String, Object>> auditType =
                  audits.stream().filter(audit -> type.equals(audit.get("type"))).findFirst();

              List<Object> events =
                  ext.containsKey("moduleAuditReports")
                      ? new ArrayList<>((List<Object>) ext.get("moduleAuditReports"))
                      : new ArrayList<>(Arrays.asList(ext));
              if (auditType.isPresent()) {
                ((List<Object>) auditType.get().get("events")).addAll(events);
              } else {
                audits.add(Map.of("type", type, "events", events));
              }
            });
    return audits;
  }

  @SneakyThrows
  private AuditReportDocument convertFromString(String sourceAsString) {
    return objectMapper.readValue(sourceAsString, AuditReportDocument.class);
  }

  public String getKvFieldValueName(SearchType type) {
    if (SearchType.INTEGER.equals(type)) {
      return FIELD_KV_NESTED_VALUE_INTEGER;
    } else if (SearchType.FLOAT.equals(type)) {
      return FIELD_KV_NESTED_VALUE_FLOAT;
    } else if (SearchType.DATE.equals(type)) {
      return FIELD_KV_NESTED_VALUE_DATE;
    }
    return FIELD_KV_NESTED_VALUE_STRING;
  }
}
