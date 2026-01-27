package org.kockpit.audit.stream.kinesis.coordination;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class LeaseHeartbeatService {

    private final DynamoDbShardCoordinator coordinator;
    private final Set<String> ownedShards = ConcurrentHashMap.newKeySet();

    public void addShard(String shardId) {
        ownedShards.add(shardId);
        log.debug("✅ Added shard {} to heartbeat monitoring", shardId);
    }

    public void removeShard(String shardId) {
        ownedShards.remove(shardId);
        log.debug("✅ Removed shard {} from heartbeat monitoring", shardId);
    }

    public Set<String> getOwnedShards() {
        return Set.copyOf(ownedShards);
    }

    @Scheduled(fixedRate = 10000) // Renew leases every 10 seconds
    public void renewLeases() {
        if (ownedShards.isEmpty()) {
            return;
        }

        log.debug("🔄 Renewing leases for {} shards", ownedShards.size());

        CompletableFuture<?>[] renewalFutures = ownedShards.stream()
                .map(shardId -> coordinator.renewLease(shardId)
                        .exceptionally(throwable -> {
                            log.warn("⚠️ Failed to renew lease for shard {}, removing from owned shards", shardId);
                            removeShard(shardId);
                            return null;
                        }))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(renewalFutures)
                .thenRun(() -> log.debug("✅ Completed lease renewal for {} shards", ownedShards.size()))
                .exceptionally(throwable -> {
                    log.error("❌ Error during lease renewal: {}", throwable.getMessage());
                    return null;
                });
    }

    public void shutdown() {
        log.info("🛑 Shutting down lease heartbeat service, releasing {} shards", ownedShards.size());

        CompletableFuture<?>[] releaseFutures = ownedShards.stream()
                .map(coordinator::releaseLease)
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(releaseFutures)
                .thenRun(() -> {
                    ownedShards.clear();
                    log.info("✅ Successfully released all leases");
                })
                .exceptionally(throwable -> {
                    log.error("❌ Error releasing leases during shutdown: {}", throwable.getMessage());
                    return null;
                })
                .join(); // Wait for completion during shutdown
    }
}