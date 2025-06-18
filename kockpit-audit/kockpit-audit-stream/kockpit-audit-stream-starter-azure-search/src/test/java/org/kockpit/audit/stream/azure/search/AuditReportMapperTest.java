package org.kockpit.audit.stream.azure.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kockpit.audit.stream.api.Audit;
import org.kockpit.audit.stream.api.AuditReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@ExtendWith(SpringExtension.class)
@Import(AuditReportMapperImpl.class)
class AuditReportMapperTest {

    @Autowired
    AuditReportMapper mapper;

    @Test
    void on_audit_report() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT);

        AuditReport auditReport = objectMapper.readValue(getClass().getResourceAsStream("/audit.json"), AuditReport.class);

        SearchAuditReport map = mapper.map(auditReport);
        log.info("Mapped Audit report {}", objectMapper
                .writeValueAsString(map));
        Assertions.assertEquals("rcu-api", map.getAppId());

    }

}