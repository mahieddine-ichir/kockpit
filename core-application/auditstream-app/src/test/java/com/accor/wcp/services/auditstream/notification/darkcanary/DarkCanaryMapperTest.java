package com.accor.wcp.services.auditstream.notification.darkcanary;

import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.darkcanary.mappings.DarkCanaryMapper;
import com.accor.wcp.services.auditstream.notification.darkcanary.mappings.DarkCanaryMapperImpl;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.DarkCanaryIndexDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

@ExtendWith(SpringExtension.class)
@Import(DarkCanaryMapperImpl.class)
class DarkCanaryMapperTest {

    @Autowired
    DarkCanaryMapper darkCanaryMapper;

    @Test
    void testDarkCanaryMapper() throws IOException {

        AuditReportRequest auditReport = new ObjectMapper().registerModule(new JavaTimeModule())
                .readValue(getClass().getResourceAsStream("/darkcanary_testing/audit-sample.json"), AuditReportRequest.class);

        DarkCanaryIndexDocument darkCanaryIndexDocument = darkCanaryMapper.toDarkCanaryIndexDocument(auditReport);

        Assertions.assertEquals("97cf0f10-119e-434b-b14d-5f0ac4ce0d6e", darkCanaryIndexDocument.getRequestId());
        Assertions.assertEquals("wcpsamples", darkCanaryIndexDocument.getAppId());
        Assertions.assertEquals("Hello Cyril !", darkCanaryIndexDocument.getResponseLeft());
        Assertions.assertEquals("3e1f4faa40a69667e9f50357c195454b", darkCanaryIndexDocument.getTraceId());

        Assertions.assertFalse(darkCanaryIndexDocument.getEndpoints().isEmpty());
        darkCanaryIndexDocument.getEndpoints()
                .forEach(endpoint -> {
                    Assertions.assertEquals("dev", endpoint.getEnv());
                    Assertions.assertEquals("/wcpsamples/sayHello/Cyril", endpoint.getUri());
                });
    }
}