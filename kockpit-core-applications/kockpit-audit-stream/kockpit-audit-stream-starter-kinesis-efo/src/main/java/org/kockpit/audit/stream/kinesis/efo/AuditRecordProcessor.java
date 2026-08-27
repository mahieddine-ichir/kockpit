package org.kockpit.audit.stream.kinesis.efo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditConsumerEvent;
import org.kockpit.audit.stream.api.AuditStreamJson;
import org.kockpit.audit.stream.api.model.AuditReport;
import org.springframework.context.ApplicationEventPublisher;
import software.amazon.kinesis.exceptions.InvalidStateException;
import software.amazon.kinesis.exceptions.ShutdownException;
import software.amazon.kinesis.lifecycle.events.InitializationInput;
import software.amazon.kinesis.lifecycle.events.LeaseLostInput;
import software.amazon.kinesis.lifecycle.events.ProcessRecordsInput;
import software.amazon.kinesis.lifecycle.events.ShardEndedInput;
import software.amazon.kinesis.lifecycle.events.ShutdownRequestedInput;
import software.amazon.kinesis.processor.RecordProcessorCheckpointer;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.retrieval.KinesisClientRecord;
import tools.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;

/**
 * One instance is created per owned shard (see {@link AuditRecordProcessorFactory}). KCL delivers
 * records for that shard only, handles resubscription/checkpointing infrastructure, and calls
 * {@link #leaseLost}/{@link #shardEnded}/{@link #shutdownRequested} as shard/worker lifecycle events
 * occur.
 */
@Slf4j
@RequiredArgsConstructor
class AuditRecordProcessor implements ShardRecordProcessor {

    private final EfoRecordProcessor recordDecoder;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper = AuditStreamJson.mapper();

    private String shardId;

    @Override
    public void initialize(InitializationInput initializationInput) {
        shardId = initializationInput.shardId();
        log.info("✅ Initializing record processor for shard {} @ {}", shardId, initializationInput.extendedSequenceNumber());
    }

    @Override
    public void processRecords(ProcessRecordsInput processRecordsInput) {
        for (KinesisClientRecord record : processRecordsInput.records()) {
            try {
                String json = recordDecoder.read(toByteArray(record.data()));
                AuditReport auditReport = objectMapper.readValue(json, AuditReport.class);
                applicationEventPublisher.publishEvent(new AuditConsumerEvent(auditReport));
                log.trace("Processed record from shard {}: {}", shardId, record.sequenceNumber());
            } catch (Exception e) {
                log.error("❌ Error processing record {} from shard {}: {}", record.sequenceNumber(), shardId, e.getMessage(), e);
            }
        }

        if (!processRecordsInput.records().isEmpty()) {
            checkpoint(processRecordsInput.checkpointer());
        }
    }

    @Override
    public void leaseLost(LeaseLostInput leaseLostInput) {
        log.info("⚠️ Lost lease for shard {}", shardId);
    }

    @Override
    public void shardEnded(ShardEndedInput shardEndedInput) {
        log.info("✅ Shard {} ended, checkpointing", shardId);
        try {
            shardEndedInput.checkpointer().checkpoint();
        } catch (ShutdownException | InvalidStateException e) {
            log.error("❌ Exception while checkpointing at shard end for {}: {}", shardId, e.getMessage(), e);
        }
    }

    @Override
    public void shutdownRequested(ShutdownRequestedInput shutdownRequestedInput) {
        log.info("🛑 Shutdown requested, checkpointing shard {}", shardId);
        checkpoint(shutdownRequestedInput.checkpointer());
    }

    private void checkpoint(RecordProcessorCheckpointer checkpointer) {
        try {
            checkpointer.checkpoint();
        } catch (ShutdownException | InvalidStateException e) {
            log.warn("⚠️ Failed to checkpoint shard {}: {}", shardId, e.getMessage());
        }
    }

    private static byte[] toByteArray(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }
}
