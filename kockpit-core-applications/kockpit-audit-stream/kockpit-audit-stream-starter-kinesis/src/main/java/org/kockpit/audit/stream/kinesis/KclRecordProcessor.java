package org.kockpit.audit.stream.kinesis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditConsumerEvent;
import org.kockpit.audit.stream.api.model.AuditReport;
import org.springframework.context.ApplicationEventPublisher;
import software.amazon.kinesis.exceptions.InvalidStateException;
import software.amazon.kinesis.exceptions.ShutdownException;
import software.amazon.kinesis.lifecycle.events.*;
import software.amazon.kinesis.processor.ShardRecordProcessor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

@Slf4j
@RequiredArgsConstructor
public class KclRecordProcessor implements ShardRecordProcessor {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());

    private String shardId;

    @Override
    public void initialize(InitializationInput initializationInput) {
        shardId = initializationInput.shardId();
        log.info("✅ Initialized processor for shard: {}", shardId);
    }

    @Override
    public void processRecords(ProcessRecordsInput processRecordsInput) {
        log.debug("📦 Processing {} records for shard: {}",
                 processRecordsInput.records().size(), shardId);

        try {
            processRecordsInput.records().forEach(record -> {
                String event = null;
                try {
                    byte[] data = new byte[record.data().remaining()];
                    record.data().get(data);
                    event = read(data);

                    AuditReport auditReport = objectMapper.readValue(event, AuditReport.class);
                    applicationEventPublisher.publishEvent(new AuditConsumerEvent(auditReport));

                    log.debug("✅ Processed record: {}", record.sequenceNumber());
                } catch (Exception e) {
                    log.error("❌ Error processing record {}: {}", record.sequenceNumber(), event, e);
                }
            });

            // Checkpoint after processing batch
            processRecordsInput.checkpointer().checkpoint();
            log.debug("✅ Checkpointed shard: {}", shardId);

        } catch (InvalidStateException | ShutdownException e) {
            log.error("❌ Checkpoint error for shard {}: {}", shardId, e.getMessage(), e);
        }
    }

    @Override
    public void leaseLost(LeaseLostInput leaseLostInput) {
        log.info("⚠️ Lease lost for shard: {}", shardId);
    }

    @Override
    public void shardEnded(ShardEndedInput shardEndedInput) {
        log.info("🔚 Shard ended: {}", shardId);
        try {
            shardEndedInput.checkpointer().checkpoint();
            log.info("✅ Final checkpoint for shard: {}", shardId);
        } catch (InvalidStateException | ShutdownException e) {
            log.error("❌ Final checkpoint failed for shard {}: {}", shardId, e.getMessage(), e);
        }
    }

    @Override
    public void shutdownRequested(ShutdownRequestedInput shutdownRequestedInput) {
        log.info("🛑 Shutdown requested for shard: {}", shardId);
        try {
            shutdownRequestedInput.checkpointer().checkpoint();
            log.info("✅ Shutdown checkpoint for shard: {}", shardId);
        } catch (InvalidStateException | ShutdownException e) {
            log.error("❌ Shutdown checkpoint failed for shard {}: {}", shardId, e.getMessage(), e);
        }
    }

    String read(byte[] message) throws IOException {
        // Check if the data is GZIP compressed by checking the magic number
        // GZIP files start with 0x1f 0x8b
        if (message.length >= 2 && message[0] == (byte) 0x1f && message[1] == (byte) 0x8b) {
            log.debug("Detected GZIP compressed data, decompressing...");
            return decompress(message);
        } else {
            log.debug("Data is not compressed, converting directly to string");
            return new String(message, StandardCharsets.UTF_8);
        }
    }

    private String decompress(byte[] compressedData) throws IOException {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipStream = new GZIPInputStream(byteStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }

            byte[] decompressed = outputStream.toByteArray();
            log.debug("Decompressed {} bytes to {} bytes", compressedData.length, decompressed.length);

            return new String(decompressed, StandardCharsets.UTF_8);
        }
    }
}