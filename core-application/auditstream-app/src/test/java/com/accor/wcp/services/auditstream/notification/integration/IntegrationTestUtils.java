package com.accor.wcp.services.auditstream.notification.integration;

import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IntegrationTestUtils {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    OBJECT_MAPPER.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
    OBJECT_MAPPER.registerModule(new Jdk8Module());
  }

  @SneakyThrows
  public static AuditReportRequest buildAuditReportRequest(final String jsonPath) {
    return OBJECT_MAPPER.readValue(loadJsonIntoString(jsonPath), AuditReportRequest.class);
  }

  private String loadJsonIntoString(final String jsonPath) throws Exception {
    final URI testFile =
        Thread.currentThread().getContextClassLoader().getResource(jsonPath).toURI();
    return String.join("", Files.readAllLines(Paths.get(testFile)));
  }
}
