package org.kockpit.audit.stream.kinesis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditConsumerEvent;
import org.kockpit.audit.stream.api.model.AuditReport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.*;
import software.amazon.awssdk.services.kinesis.model.Record;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

@Slf4j
@RequiredArgsConstructor
public class KinesisStreamListener {
    
    private final KinesisClient kinesisClient;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final ObjectMapper objectMapper;

    @Value("${kockpit.audit.stream.kinesis.streamName:auditstream-local}")
    private String streamName;

    private volatile boolean running = false;
    
    @Async  // Runs in separate thread
    public void startAsync() {
        running = true;
        log.info("✅ Listener started");
        
        try {
            ListShardsResponse response = kinesisClient.listShards(
                ListShardsRequest.builder()
                    .streamName(streamName)
                    .build()
            );

            // Listen to shards
            for (Shard shard : response.shards()) {
                listenToShard(streamName, shard.shardId());
            }
            
        } catch (Exception e) {
            log.error("❌ Error: {}", e.getMessage(), e);
            running = false;
        }
    }
    
    private void listenToShard(String streamName, String shardId) {
        log.info("Listening to stream: {}, at shard: {}", streamName, shardId);
        long iteratorStartTime = System.currentTimeMillis();
        String shardIterator = getShardIterator(streamName, shardId);
        while (running) {
            try {
                // Refresh iterator every 4 minutes (before 5 minute expiry)
                if (System.currentTimeMillis() - iteratorStartTime > 240_000 || shardIterator == null) {
                    log.info("🔄 Getting fresh iterator");
                    shardIterator = getShardIterator(streamName, shardId);
                    iteratorStartTime = System.currentTimeMillis();
                }

                GetRecordsResponse response = kinesisClient.getRecords(
                        GetRecordsRequest.builder()
                                .shardIterator(shardIterator)
                                .limit(100)
                                .build()
                );

                // Process records
                this.processRecords(response.records());

                shardIterator = response.nextShardIterator();
                Thread.sleep(1000);

            } catch (ExpiredIteratorException e) {
                log.warn("⏰ Iterator expired, getting fresh one ...");
                shardIterator = null;  // Force refresh
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    break;
                }
            } catch (InterruptedException e) {
                log.info("Listener interrupted");
                break;
            } catch (Exception e) {
                log.error("❌ Error streaming data {}", e.getMessage(), e);
                break;
            }
        }
    }

    private void processRecords(Collection<Record> records) {
        records.forEach(record -> {
            String event = null;
            try {
                event = read(record.data().asByteArray());
                AuditReport auditReport = objectMapper.readValue(event, AuditReport.class);
                applicationEventPublisher.publishEvent(new AuditConsumerEvent(auditReport));
            } catch (Exception e) {
                log.error("❌ Error processing audit record {}", event, e);
            }
        });
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

    private String getShardIterator(String streamName, String shardId) {
        try {
            GetShardIteratorResponse response = kinesisClient.getShardIterator(
                    GetShardIteratorRequest.builder()
                            .streamName(streamName)
                            .shardId(shardId)
                            .shardIteratorType("LATEST")
                            .build()
            );

            log.info("✅ Got shard iterator for {}", shardId);
            return response.shardIterator();

        } catch (Exception e) {
            log.error("❌ Failed to get iterator: {}", e.getMessage(), e);
            return null;
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        log.info("⏹️ Stopped");
    }
}
