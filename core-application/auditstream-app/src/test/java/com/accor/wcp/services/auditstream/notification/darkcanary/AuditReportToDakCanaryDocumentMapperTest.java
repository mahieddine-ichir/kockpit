package com.accor.wcp.services.auditstream.notification.darkcanary;

import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.ConfigurationProvider;
import com.accor.wcp.services.auditstream.notification.darkcanary.mappings.AuditReportToDakCanaryDocumentMapper;
import com.accor.wcp.services.auditstream.notification.darkcanary.mappings.DarkCanaryMapperImpl;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
@Import({AuditReportToDakCanaryDocumentMapper.class, ConfigurationProvider.class, DarkCanaryMapperImpl.class, TestConfiguration.class})
class AuditReportToDakCanaryDocumentMapperTest {

    @Autowired
    private AuditReportToDakCanaryDocumentMapper auditReportToDakCanaryDocumentMapper;

    @Test
    @DisplayName("on audit report, should map all attributes exception Response_v1")
    void on_auditReport() {
        AuditReportRequest report = audit();
        ConfiguredDarkCanaryIndexDocument configuredDarkCanaryIndexDocument = auditReportToDakCanaryDocumentMapper.apply(new ConfiguredAuditReportRequest(report, null));
        DarkCanaryIndexDocument darkCanaryIndexDocument = configuredDarkCanaryIndexDocument.darkCanaryIndexDocument();

        KeyValuePair keyValuePair = darkCanaryIndexDocument.getId().get(0);
        List<Endpoint> endpoints = darkCanaryIndexDocument.getEndpoints();

        Assertions.assertEquals(1, darkCanaryIndexDocument.getId().size());
        Assertions.assertEquals("X-B3-TraceId", keyValuePair.getKey());
        Assertions.assertEquals("3e1f4faa40a69667e9f50357c195454b", keyValuePair.getValue());
        Assertions.assertEquals("wcpsamples", darkCanaryIndexDocument.getAppId());
        Assertions.assertEquals("wcplatform", darkCanaryIndexDocument.getDomain());
        Assertions.assertEquals("Hello Cyril !", darkCanaryIndexDocument.getResponseLeft());
        Assertions.assertEquals(1, endpoints.size());
        Assertions.assertEquals("dev", endpoints.get(0).getEnv());
        Assertions.assertEquals("/wcpsamples/sayHello/Cyril", endpoints.get(0).getUri());

        Assertions.assertNull(darkCanaryIndexDocument.getResponseRight());
    }

    @SneakyThrows
    private AuditReportRequest audit() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        return objectMapper.readValue(this.getClass().getResourceAsStream("/darkcanary_testing/audit-sample.json"), AuditReportRequest.class);
    }
}