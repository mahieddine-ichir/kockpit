package org.kockpit.audit.stream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kockpit.audit.stream.api.model.AuditReport;

import java.io.IOException;

/**
 * Valide le contrat de deserialisation audit.json -> AuditReport avec le mapper Jackson 2
 * configure comme dans KafkaStreamAutoConfiguration (le chemin reel de consommation),
 * et non le bean ObjectMapper auto-configure par Boot (Jackson 3 depuis Boot 4).
 */
public class SerdesTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    void on_audit_json() throws IOException {
        AuditReport auditReport = objectMapper.readValue(this.getClass().getResourceAsStream("/audit.json"), AuditReport.class);
        Assertions.assertThat(auditReport).isNotNull();
        Assertions.assertThat(auditReport.getId()).isEqualTo("f945150f-dd07-42df-aa69-6696f75c395e");
        Assertions.assertThat(auditReport.getIndexedKeyValues()).hasSize(6);
        Assertions.assertThat(auditReport.getAudits()).hasSize(1);
        Assertions.assertThat(auditReport.getAudits().iterator().next().getType()).isEqualTo("builtin.web");
    }

}
