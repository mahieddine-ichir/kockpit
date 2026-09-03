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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * accept() is the backpressure gate an OOM (S3 or OpenSearch falling behind ingestion, see
 * onS3Write's synchronous indexing call) needs: once buffered-but-not-yet-written bytes reach
 * maxBufferedBytes, it must block the caller - not accept-then-reject - until S3/OpenSearch drain
 * the buffer. Blocking (not returning) is what actually holds up a push-based Kinesis EFO
 * subscription's next checkpoint; an exception here would just get caught upstream and let the
 * subscription (and KCL's checkpoint ceiling) advance regardless, silently skipping whatever was
 * rejected in between.
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
    @DisplayName("Une fois la limite d'octets atteinte, accept() bloque au lieu de rejeter")
    void blocks_new_records_once_the_byte_limit_is_reached() throws InterruptedException {
        consumer.accept(List.of(record));

        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(1);
        Thread blockedAccept = new Thread(() -> {
            started.countDown();
            try {
                consumer.accept(List.of(record));
            } catch (S3ConsumerBackpressureException expected) {
                // Interrupted below to unwind the thread - not the behavior under test here.
            }
            finished.countDown();
        });
        blockedAccept.start();

        assertThat(started.await(2, TimeUnit.SECONDS)).as("accept() thread started").isTrue();
        // Still blocked - nothing has drained the buffer yet.
        assertThat(finished.await(300, TimeUnit.MILLISECONDS)).as("accept() returned too early").isFalse();

        blockedAccept.interrupt();
        blockedAccept.join(2000);
    }

    @Test
    @DisplayName("Un flush qui vide le buffer debloque un accept() en attente")
    void a_drain_unblocks_a_waiting_accept() throws InterruptedException {
        consumer.accept(List.of(record));

        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(1);
        Thread blockedAccept = new Thread(() -> {
            started.countDown();
            consumer.accept(List.of(record));
            finished.countDown();
        });
        blockedAccept.start();

        assertThat(started.await(2, TimeUnit.SECONDS)).as("accept() thread started").isTrue();
        assertThat(finished.await(300, TimeUnit.MILLISECONDS)).as("accept() returned too early").isFalse();

        // Drains the first (only buffered) record, freeing exactly enough room for the blocked
        // second one to proceed.
        consumer.flush();
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        assertThat(finished.await(2, TimeUnit.SECONDS)).as("blocked accept() unblocked after drain").isTrue();
        blockedAccept.join(2000);
    }
}
