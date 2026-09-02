package org.kockpit.audit.stream.s3;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditStreamJson;
import org.kockpit.audit.stream.api.model.AuditReport;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Batches consumed {@link AuditReport}s in memory and periodically writes them to S3, grouped by
 * domain/env/appId ({@link S3AuditGroup}) so each object holds reports from a single group, one
 * newline-delimited JSON object per group per flush. Records are serialized with the same
 * {@link AuditStreamJson} mapper used to read the stream, so the archive stays byte-compatible
 * with the wire format.
 */
@Slf4j
@RequiredArgsConstructor
public class S3AuditConsumer {

    // producer (accept) / consumer (flush) can run on different threads (e.g. one per Kinesis
    // shard); a plain ArrayList would not be safe to share across them.
    //private final Queue<List<S3Record>> auditReports = new ConcurrentLinkedQueue<>();
    //private final Map<S3Key, Queue<S3Record>> auditReports = new ConcurrentHashMap<>();
    private final Map<S3Key, S3Batch> auditReports = new ConcurrentHashMap<>();

    private final S3Client s3Client;

    private final String bucketName;

    private final Integer batchSize;

    private final Integer ttlDefaultInDays;

    // The bucket's S3 lifecycle rules only expire objects tagged with one of these exact
    // "retention=<days>" values - anything else matches no rule and never gets cleaned up. Sorted
    // ascending on construction so snapToAllowedTtl() can rely on ordering regardless of how the
    // config lists them.
    private final List<Integer> allowedTtlDays;

    private final ApplicationEventPublisher eventPublisher;

    @PostConstruct
    public void start() {
        log.info("✅ S3 Audit consumer started, writing batches to bucket {}", bucketName);
        Runtime.getRuntime().addShutdownHook(new Thread(this::flush));
    }

    public void accept(List<byte[]> events) {
        events.stream().map(bytes -> new S3Record(bytes, S3Key.read(bytes)))
                .collect(Collectors.groupingBy(S3Record::getS3Key))
                .forEach((k, v) -> {
                    auditReports.putIfAbsent(k, new S3Batch());
                    S3Batch s3Batch = auditReports.get(k);
                    for (S3Record record : v) {
                        // Flushing the moment a key's queue crosses a batchSize boundary (not
                        // just on the fixed schedule) bounds how large it can grow between ticks
                        // for a hot key outpacing the scheduler - the unbounded-growth OOM this
                        // consumer hit before was exactly that, not stale keys (which
                        // pruneEmptyBatches already handles).
                        if (s3Batch.add(record) % batchSize == 0) {
                            flushKey(k);
                        }
                    }
                });
    }

    private void write(List<S3Record> batch, S3Key prefix) {
        int ttl = resolveTtl(batch);
        try {
            assignOffsets(batch);
            String s3Key = prefix.toString() + "/" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + ".batch";
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Key)
                            .tagging("retention=%d".formatted(ttl))
                            .build(),
                    RequestBody.fromBytes(concatenate(batch))
            );

            eventPublisher.publishEvent(new S3WriteEvent(this, s3Key, batch, bucketName));
            log.trace("Wrote {} audit reports to s3://{}/{}", batch.size(), bucketName, s3Key);
        } catch (Exception e) {
            log.error("❌ Failed to write {} audit reports to s3://{}/{}: {}", batch.size(), bucketName, prefix, e.getMessage(), e);
        }
    }

    // One S3 object can hold reports from several AuditReports with different ttl values (S3
    // lifecycle/retention tagging applies per-object, not per-record), so the object's retention
    // must cover the longest-lived report in it - hence the max, not e.g. the first record's ttl.
    private int resolveTtl(List<S3Record> batch) {
        int ttl = batch.stream()
                .mapToInt(this::readTtl)
                .max()
                .orElse(ttlDefaultInDays);
        return snapToAllowedTtl(ttl);
    }

    // Rounds UP to the smallest configured value that still covers the requested retention (never
    // down - under-retaining audit data is worse than over-retaining it), capping at the longest
    // available bucket if the request exceeds all of them.
    private int snapToAllowedTtl(int requestedDays) {
        List<Integer> sorted = allowedTtlDays.stream().sorted().toList();
        for (int allowed : sorted) {
            if (requestedDays <= allowed) {
                return allowed;
            }
        }
        int longest = sorted.get(sorted.size() - 1);
        log.warn("⚠️ Requested ttl {}d exceeds the longest available retention bucket ({}d), capping to it",
                requestedDays, longest);
        return longest;
    }

    // record.getS3Key().getTtl() was already parsed once from this record's bytes in accept()
    // (S3Key.read) - no need to decompress/parse the full AuditReport a second time just for ttl.
    private int readTtl(S3Record record) {
        Integer ttl = record.getS3Key().getTtl();
        return ttl != null ? ttl : ttlDefaultInDays;
    }

    // Position/length within THIS specific object, not cumulative across the key's lifetime -
    // each flush() writes a brand-new, separate S3 object, so offsets must restart at 0 here.
    private static void assignOffsets(List<S3Record> batch) {
        long offset = 0;
        for (S3Record record : batch) {
            record.setOffset(offset);
            record.setLength(record.getData().length);
            offset += record.getData().length;
        }
    }

    static byte[] concatenate(List<S3Record> data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        data.stream().map(S3Record::getData).forEach(out::writeBytes);
        return out.toByteArray();
    }

    @Scheduled(
            fixedDelayString = "${kockpit.audit.stream.s3.scheduler_ms:5000}",
            timeUnit = TimeUnit.MILLISECONDS)
    void flush() {
        for (S3Key key : auditReports.keySet()) {
            flushKey(key);
        }
        pruneEmptyBatches();
    }

    // Swaps out whatever's queued for this key and writes up to batchSize of it, requeuing any
    // leftover. Called both for every key on the fixed schedule, and inline from accept() the
    // instant a key's queue crosses a batchSize boundary, so a hot key can't outgrow batchSize
    // between ticks.
    private void flushKey(S3Key key) {
        S3Batch oldBatch = swapForFreshBatch(key);
        if (oldBatch == null) {
            return;
        }
        // Cap this write at batchSize, same as before - anything beyond that stays queued
        // (requeued below) rather than growing one S3 object without bound.
        List<S3Record> batch = new ArrayList<>();
        S3Record record;
        for (int i = 0; i < batchSize && (record = oldBatch.poll()) != null; i++) {
            batch.add(record);
        }
        // Requeue any leftover beyond batchSize onto whatever's currently there for this key -
        // may already hold records accept() added after the swap below.
        S3Record leftover;
        while ((leftover = oldBatch.poll()) != null) {
            auditReports.computeIfAbsent(key, k -> new S3Batch()).add(leftover);
        }
        write(batch, key);
    }

    // Atomically replaces this key's batch with a fresh empty one and returns the batch being
    // replaced, exclusively ours from this point on - any accept() call that already holds a
    // reference to it (mid-add) still completes safely (ConcurrentLinkedQueue is thread-safe for
    // that), and any accept() call after this swap gets the new instance instead. Closes the
    // accept()/flush() race that a drain-in-place poll() loop has: no window where a record can
    // land in a batch this cycle has already decided is empty and is about to prune.
    private S3Batch swapForFreshBatch(S3Key key) {
        S3Batch[] previous = new S3Batch[1];
        auditReports.compute(key, (k, current) -> {
            previous[0] = current;
            return new S3Batch();
        });
        return (previous[0] == null || previous[0].isEmpty()) ? null : previous[0];
    }

    // Without this, auditReports grows without bound for the lifetime of the process - one entry
    // per distinct (domain, env, appId, ttl) combination ever seen. On a shared, multi-tenant
    // stream with ephemeral per-MR review environments constantly rotating through, that's
    // effectively unbounded growth (the direct cause of an eventual "Java heap space" OOM,
    // regardless of how much heap is available - it only changes how long it takes).
    private void pruneEmptyBatches() {
        auditReports.keySet().forEach(key ->
                auditReports.computeIfPresent(key, (k, batch) -> batch.isEmpty() ? null : batch));
    }
}
