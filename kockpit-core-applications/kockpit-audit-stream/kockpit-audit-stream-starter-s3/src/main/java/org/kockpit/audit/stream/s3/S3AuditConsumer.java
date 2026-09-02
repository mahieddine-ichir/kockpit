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
                        s3Batch.add(record);
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
            log.error("❌ Failed to write {} audit reports to s3://{}/{}: {}", batch.size(), bucketName, s3Client, e.getMessage(), e);
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
        for (Map.Entry<S3Key, S3Batch> entry : auditReports.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            List<S3Record> batch = new ArrayList<>();
            S3Record report;
            for (int i = 0; i < batchSize && (report = entry.getValue().poll()) != null; i++) {
                batch.add(report);
            }
            write(batch, entry.getKey());
        }
        pruneEmptyBatches();
    }

    // Without this, auditReports grows without bound for the lifetime of the process - one entry
    // per distinct (domain, env, appId, ttl) combination ever seen. On a shared, multi-tenant
    // stream with ephemeral per-MR review environments constantly rotating through, that's
    // effectively unbounded growth (the direct cause of an eventual "Java heap space" OOM,
    // regardless of how much heap is available - it only changes how long it takes).
    //
    // computeIfPresent keeps this safe against accept() concurrently creating the SAME key
    // (ConcurrentHashMap serializes compute-family/putIfAbsent calls per key); accept()'s
    // get()-then-add() on an already-retrieved batch reference isn't covered by that, so a record
    // arriving in the exact instant a now-empty batch is being pruned could still be lost - rare,
    // and far preferable to the guaranteed eventual OOM this replaces.
    private void pruneEmptyBatches() {
        auditReports.keySet().forEach(key ->
                auditReports.computeIfPresent(key, (k, batch) -> batch.isEmpty() ? null : batch));
    }
}
