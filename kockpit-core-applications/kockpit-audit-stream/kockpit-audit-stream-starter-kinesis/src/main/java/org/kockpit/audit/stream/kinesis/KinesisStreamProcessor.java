package org.kockpit.audit.stream.kinesis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.kockpit.audit.stream.kinesis.coordination.DynamoDbShardCoordinator;
import org.kockpit.audit.stream.kinesis.coordination.LeaseHeartbeatService;
import org.springframework.scheduling.annotation.Scheduled;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class KinesisStreamProcessor {

    private final KinesisAsyncClient kinesisAsyncClient;

    private final String streamName;

    private final String applicationName;

    private final KclRecordProcessor kclRecordProcessor;

    private final List<AuditConsumer> auditConsumers;

    private final int recordLimit;

    private final DynamoDbShardCoordinator shardCoordinator;

    private final LeaseHeartbeatService heartbeatService;

    private final long shardAcquisitionIntervalMs;

    private final Map<String, String> shardIterators = new ConcurrentHashMap<>();

    private volatile boolean initialized = false;
    private final AtomicLong lastShardAcquisition = new AtomicLong(0);

    @Scheduled(fixedDelayString = "${kockpit.audit.stream.kinesis.read_interval_ms:5000}")
    public void read() {
        if (!initialized) {
            initializeCoordination();
            return;
        }

        // Only acquire new shards periodically or on first run
        long now = System.currentTimeMillis();
        if (now - lastShardAcquisition.get() > shardAcquisitionIntervalMs) {
            acquireAvailableShards();
            lastShardAcquisition.set(now);
        }

        // Only read from shards we own
        Set<String> ownedShards = heartbeatService.getOwnedShards();
        log.trace("Reading records from {} owned shards", ownedShards.size());

        ownedShards.forEach(shardId -> {
            String iterator = shardIterators.get(shardId);
            if (iterator != null) {
                readFromShard(shardId, iterator);
            }
        });
    }

    private void initializeCoordination() {
        log.info("✅ Initializing DynamoDB coordination for stream: {} with application: {}", streamName, applicationName);

        shardCoordinator.ensureTableExists()
                .thenCompose(v -> {
                    // Add a small delay to ensure table is fully ready
                    return CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(2000); // 2 second delay
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                })
                .thenRun(() -> {
                    initialized = true;
                    // Trigger immediate shard acquisition after table is ready
                    lastShardAcquisition.set(0);
                    log.info("✅ DynamoDB coordination initialized successfully");
                })
                .exceptionally(throwable -> {
                    log.error("❌ Failed to initialize DynamoDB coordination: {}", throwable.getMessage(), throwable);
                    return null;
                });
    }

    private void acquireAvailableShards() {
        List<Shard> allShards = listKinShards();
        List<String> allShardIds = allShards.stream()
                .map(Shard::shardId)
                .collect(Collectors.toList());

        shardCoordinator.acquireAvailableShards(allShardIds)
                .thenAccept(acquiredShards -> {
                    // Add newly acquired shards to heartbeat and initialize iterators
                    acquiredShards.forEach(shardId -> {
                        if (!heartbeatService.getOwnedShards().contains(shardId)) {
                            heartbeatService.addShard(shardId);
                            initializeShardIterator(shardId);
                            log.trace("✅ Acquired and initialized shard: {}", shardId);
                        }
                    });

                    // Remove shards we no longer own
                    Set<String> ownedShards = heartbeatService.getOwnedShards();
                    Set<String> acquiredShardSet = Set.copyOf(acquiredShards);
                    ownedShards.stream()
                            .filter(shardId -> !acquiredShardSet.contains(shardId))
                            .forEach(shardId -> {
                                heartbeatService.removeShard(shardId);
                                shardIterators.remove(shardId);
                                log.trace("⚠️ Lost ownership of shard: {}", shardId);
                            });
                })
                .exceptionally(throwable -> {
                    log.error("❌ Failed to acquire shards: {}", throwable.getMessage());
                    return null;
                });
    }

    private void initializeShardIterator(String shardId) {
        GetShardIteratorRequest shardIteratorRequest = GetShardIteratorRequest.builder()
                .streamName(streamName)
                .shardIteratorType(ShardIteratorType.LATEST)
                .shardId(shardId)
                .build();

        kinesisAsyncClient.getShardIterator(shardIteratorRequest)
                .thenAccept(response -> {
                    shardIterators.put(shardId, response.shardIterator());
                    log.info("✅ Initialized iterator for shard: {}", shardId);
                })
                .exceptionally(throwable -> {
                    log.error("❌ Failed to initialize iterator for shard: {}", shardId, throwable);
                    return null;
                });
    }

    private void readFromShard(String shardId, String iterator) {
        GetRecordsRequest recordsRequest = GetRecordsRequest.builder()
                .shardIterator(iterator)
                .limit(recordLimit)
                .build();

        kinesisAsyncClient.getRecords(recordsRequest).thenAccept(records -> {
            List<byte[]> list = records.records().stream().map(record -> kclRecordProcessor.read(record.data().asByteArray()))
                    .map(String::getBytes)
                    .toList();

            auditConsumers.forEach(auditConsumer -> auditConsumer.accept(list));

            // Update iterator for next read - this is crucial!
            String nextIterator = records.nextShardIterator();
            if (nextIterator != null) {
                shardIterators.put(shardId, nextIterator);
            } else {
                log.warn("⚠️ No next iterator for shard: {}, removing from active shards", shardId);
                shardIterators.remove(shardId);
            }

            if (!records.records().isEmpty()) {
                log.trace("Processed {} records from shard: {}", records.records().size(), shardId);
            }
        }).exceptionally(throwable -> {
            log.error("❌ Error reading from shard: {}", shardId, throwable);
            // Handle iterator expiration by re-initializing
            if (throwable.getMessage() != null && throwable.getMessage().contains("Iterator")) {
                log.warn("⚠️ Iterator expired for shard: {}, will re-initialize", shardId);
                shardIterators.remove(shardId);
                reinitializeShard(shardId);
            }
            return null;
        });
    }

    private void reinitializeShard(String shardId) {
        GetShardIteratorRequest shardIteratorRequest = GetShardIteratorRequest.builder()
                .streamName(streamName)
                .shardIteratorType(ShardIteratorType.LATEST)
                .shardId(shardId)
                .build();

        kinesisAsyncClient.getShardIterator(shardIteratorRequest).thenAccept(response -> {
            shardIterators.put(shardId, response.shardIterator());
            log.trace("✅ Re-initialized iterator for shard: {}", shardId);
        }).exceptionally(throwable -> {
            log.error("❌ Failed to re-initialize iterator for shard: {}", shardId, throwable);
            return null;
        });
    }

    private List<Shard> listKinShards() {
        try {
            ListShardsRequest request = ListShardsRequest.builder()
                    .streamName(streamName)
                    .build();

            ListShardsResponse response = kinesisAsyncClient.listShards(request).get();
            log.trace("✅ {} has {}", request.streamName(), response.shards());
            return response.shards();
        } catch (Exception e) {
            log.error("❌ Error starting Kinesis stream processing", e);
            throw new RuntimeException(e);
        }
    }
}
