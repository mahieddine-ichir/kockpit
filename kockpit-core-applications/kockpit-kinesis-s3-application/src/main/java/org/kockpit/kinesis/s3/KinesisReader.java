package org.kockpit.kinesis.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

@RequiredArgsConstructor
@Slf4j
public class KinesisReader {

    private final KinesisClient client;
    private final String streamName;
    private final int recordLimit;
    private final Consumer<String> recordHandler;

    private final Map<String, String> shardIterators = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;

    public void poll() {
        if (!initialized) {
            initialize();
            return;
        }
        shardIterators.forEach((shardId, iterator) -> {
            if (iterator != null) {
                readFromShard(shardId, iterator);
            }
        });
    }

    private void initialize() {
        try {
            List<Shard> shards = client.listShards(ListShardsRequest.builder()
                    .streamName(streamName)
                    .build()).shards();

            shards.forEach(shard -> {
                GetShardIteratorResponse resp = client.getShardIterator(
                        GetShardIteratorRequest.builder()
                                .streamName(streamName)
                                .shardId(shard.shardId())
                                .shardIteratorType(ShardIteratorType.TRIM_HORIZON)
                                .build());
                shardIterators.put(shard.shardId(), resp.shardIterator());
                log.info("Initialized iterator for shard {}", shard.shardId());
            });

            initialized = true;
            log.info("KinesisReader initialized on stream {}, {} shard(s)", streamName, shards.size());
        } catch (Exception e) {
            log.error("Failed to initialize KinesisReader: {}", e.getMessage(), e);
        }
    }

    private void readFromShard(String shardId, String iterator) {
        try {
            GetRecordsResponse response = client.getRecords(GetRecordsRequest.builder()
                    .shardIterator(iterator)
                    .limit(recordLimit)
                    .build());

            response.records().forEach(record -> {
                try {
                    byte[] bytes = record.data().asByteArray();
                    String json = isGzip(bytes) ? decompress(bytes) : new String(bytes, StandardCharsets.UTF_8);
                    recordHandler.accept(json);
                } catch (Exception e) {
                    log.error("Error processing record {}: {}", record.sequenceNumber(), e.getMessage(), e);
                }
            });

            if (response.nextShardIterator() != null) {
                shardIterators.put(shardId, response.nextShardIterator());
            } else {
                shardIterators.remove(shardId);
                log.warn("Shard {} closed", shardId);
            }
        } catch (Exception e) {
            log.error("Error reading from shard {}: {}", shardId, e.getMessage(), e);
            shardIterators.remove(shardId);
        }
    }

    private boolean isGzip(byte[] bytes) {
        return bytes.length >= 2 && bytes[0] == (byte) 0x1f && bytes[1] == (byte) 0x8b;
    }

    private String decompress(byte[] compressed) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
             GZIPInputStream gis = new GZIPInputStream(bis);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = gis.read(buf)) > 0) out.write(buf, 0, len);
            return out.toString(StandardCharsets.UTF_8);
        }
    }
}