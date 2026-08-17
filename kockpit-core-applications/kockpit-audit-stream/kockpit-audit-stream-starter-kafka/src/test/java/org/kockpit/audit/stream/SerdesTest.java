package org.kockpit.audit.stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kockpit.audit.stream.api.AuditStreamJson;
import org.kockpit.audit.stream.api.model.AuditReport;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

/**
 * Valide le contrat de deserialisation audit.json -> AuditReport avec le mapper
 * de production (AuditStreamJson, celui des starters Kafka et Kinesis),
 * et non le bean ObjectMapper auto-configure par Boot. Les Instant du flux sont
 * des timestamps numeriques (secondes.nanos) : READ_DATE_TIMESTAMPS_AS_NANOSECONDS
 * est actif par defaut en Jackson 3.
 */
public class SerdesTest {

    private final ObjectMapper objectMapper = AuditStreamJson.mapper();

    @Test
    void on_audit_json() throws IOException {
        AuditReport auditReport = objectMapper.readValue(this.getClass().getResourceAsStream("/audit.json"), AuditReport.class);
        Assertions.assertThat(auditReport).isNotNull();
        Assertions.assertThat(auditReport.getId()).isEqualTo("f945150f-dd07-42df-aa69-6696f75c395e");
        Assertions.assertThat(auditReport.getIndexedKeyValues()).hasSize(6);
        Assertions.assertThat(auditReport.getAudits()).hasSize(1);
        Assertions.assertThat(auditReport.getAudits().iterator().next().getType()).isEqualTo("builtin.web");
    }

    /**
     * Le flux transporte les Instant en secondes.nanosecondes ("start":1750197974.047081776).
     * Verrouille cette lecture : un mapper qui interpreterait le nombre en millisecondes
     * produirait des dates en 1970.
     */
    @Test
    void reads_instants_as_seconds_with_nanos() throws IOException {
        AuditReport auditReport = objectMapper.readValue(this.getClass().getResourceAsStream("/audit.json"), AuditReport.class);
        Assertions.assertThat(auditReport.getStart()).isEqualTo(Instant.ofEpochSecond(1750197974L, 47081776));
        Assertions.assertThat(auditReport.getEnd()).isEqualTo(Instant.ofEpochSecond(1750197978L, 958444601));
    }

}
