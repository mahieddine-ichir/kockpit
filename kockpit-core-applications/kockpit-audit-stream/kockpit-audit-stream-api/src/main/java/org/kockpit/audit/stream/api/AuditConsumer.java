package org.kockpit.audit.stream.api;

import org.springframework.context.SmartLifecycle;

import java.util.List;
import java.util.function.Consumer;

/**
 * accept must be implemented by classes to consume received audits.
 */
public interface AuditConsumer extends Consumer<List<byte[]>> {

    /**
     * SmartLifecycle phase for consumers that buffer in memory and must drain on shutdown. Lower
     * than the stream readers' phases (Kinesis EFO scheduler at DEFAULT_PHASE, Kafka listener
     * containers at DEFAULT_PHASE - 100), so Spring stops the readers first and only then drains
     * the consumers - and all of it happens before singleton destruction closes the S3/OpenSearch
     * clients the drain writes through. A raw JVM shutdown hook gives no such guarantee: it runs
     * concurrently with Spring's own hook and loses the race against client close.
     */
    int SHUTDOWN_DRAIN_PHASE = SmartLifecycle.DEFAULT_PHASE - 1000;

    /**
     * Synchronously writes out everything this consumer is still holding in memory. Called
     * before a shard is checkpointed on shutdown, so the checkpoint never advances past records
     * that were accepted but not yet persisted. No-op for consumers that don't buffer.
     */
    default void drain() {
    }
}
