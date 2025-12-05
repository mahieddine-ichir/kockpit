package org.kockpit.audit.stream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.kockpit.audit.stream.api.model.AuditReport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.*;
import software.amazon.awssdk.services.kinesis.model.Record;

import java.util.Collection;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class KinesisListener {
    
    private final KinesisClient kinesisClient;

    private final List<AuditConsumer> auditConsumers;

    @Value("${kockpit.audit.stream.kinesis.streamName:auditstream-local}")
    private String streamName;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());
    
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
            try {
                AuditReport auditReport = objectMapper.readValue(record.data().asByteArray(), AuditReport.class);
                auditConsumers.forEach(consumer -> consumer.accept(auditReport));
            } catch (Exception e) {
                log.error("❌ Error reading/converting record {}", record.data().asByteArray(), e);
            }
        });
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
