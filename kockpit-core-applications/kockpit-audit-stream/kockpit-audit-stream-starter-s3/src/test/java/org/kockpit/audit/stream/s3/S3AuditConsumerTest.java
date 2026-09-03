package org.kockpit.audit.stream.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kockpit.audit.stream.api.AuditStreamJson;
import org.kockpit.audit.stream.api.model.AuditReport;
import org.springframework.context.ApplicationEventPublisher;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * accept() is the backpressure gate an OOM (S3 or OpenSearch falling behind ingestion, see
 * onS3Write's synchronous indexing call) needs: once buffered-but-not-yet-written bytes reach
 * maxBufferedBytes, it must reject new records outright, not add them to auditReports, and throw
 * - so that whichever record processor called it (EFO/non-EFO Kinesis) skips its checkpoint and
 * the rejected batch gets redelivered once S3/OpenSearch drain the buffer.
 */
class S3AuditConsumerTest {

    private final ObjectMapper mapper = AuditStreamJson.mapper();
    private final S3Client s3Client = mock(S3Client.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    private byte[] record;
    private S3AuditConsumer consumer;

    @BeforeEach
    void setUp() {
        AuditReport report = new AuditReport();
        report.setId("evt-1");
        report.setDomain("rcu");
        report.setEnv("prod");
        report.setAppId("app");
        report.setTtl(1);
        record = mapper.writeValueAsBytes(report);

        // batchSize kept far above 1 so nothing here auto-flushes on a batch-size boundary -
        // these tests only exercise the byte-limit gate, not the batch-size one.
        consumer = new S3AuditConsumer(s3Client, "bucket", 1000, 1, List.of(1, 7), eventPublisher, record.length);
    }

    @Test
    @DisplayName("Un record est accepte tant que le buffer reste sous la limite d'octets")
    void accepts_records_under_the_byte_limit() {
        consumer.accept(List.of(record));

        verifyNoInteractions(s3Client);
    }

    @Test
    @DisplayName("Une fois la limite d'octets atteinte, les nouveaux records sont rejetes (pas bufferises)")
    void rejects_new_records_once_the_byte_limit_is_reached() {
        consumer.accept(List.of(record));

        assertThatThrownBy(() -> consumer.accept(List.of(record)))
                .isInstanceOf(S3ConsumerBackpressureException.class);
    }

    @Test
    @DisplayName("Apres un flush qui vide le buffer, de nouveaux records sont a nouveau acceptes")
    void accepts_again_once_a_flush_drains_the_buffer_below_the_limit() {
        consumer.accept(List.of(record));
        assertThatThrownBy(() -> consumer.accept(List.of(record)))
                .isInstanceOf(S3ConsumerBackpressureException.class);

        consumer.flush();
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // Rejected above, so never buffered - draining exactly the one accepted record must be
        // enough to bring the buffer back under the limit and let this succeed.
        consumer.accept(List.of(record));
    }
}
