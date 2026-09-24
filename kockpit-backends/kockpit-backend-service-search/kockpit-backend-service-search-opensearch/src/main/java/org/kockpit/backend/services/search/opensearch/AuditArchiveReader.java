package org.kockpit.backend.services.search.opensearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.zip.GZIPInputStream;

/**
 * Reads back the {@code audits} array (httpAuditedRequest/httpAuditedResponse) that
 * OpensearchS3AuditConsumer strips before indexing, archived instead to S3 (see its
 * {@code onS3Write}). {@code s3Uri} fully qualifies bucket + key on its own, so no bucket needs
 * configuring here - it works across whichever bucket/environment produced the record.
 *
 * <p>Two archive shapes exist, distinguished only by whether s3Offset/s3Size are set: a
 * standalone per-record object (producers that offload before publishing - no offset/size) and a
 * shared multi-record object one flush of {@code S3AuditConsumer} writes (offset/size mark this
 * record's byte range within it). Both are read the same way - byte range when set, the whole
 * object otherwise - then parsed as one AuditReport JSON document (possibly gzip-compressed, same
 * wire format the audit-stream consumers read).
 */
@RequiredArgsConstructor
@Slf4j
public class AuditArchiveReader {

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    public Object readAudits(String s3Uri, Long s3Offset, Long s3Size) {
        if (s3Uri == null) {
            return null;
        }
        try {
            URI uri = URI.create(s3Uri);
            String bucket = uri.getHost();
            String key = uri.getPath().startsWith("/") ? uri.getPath().substring(1) : uri.getPath();

            GetObjectRequest.Builder requestBuilder = GetObjectRequest.builder().bucket(bucket).key(key);
            if (s3Offset != null && s3Size != null) {
                requestBuilder.range("bytes=%d-%d".formatted(s3Offset, s3Offset + s3Size - 1));
            }

            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(requestBuilder.build());
            byte[] bytes = decompressIfNeeded(response.asByteArray());

            JsonNode auditReport = objectMapper.readTree(bytes);
            return objectMapper.convertValue(auditReport.get("audits"), Object.class);
        } catch (Exception e) {
            log.warn("Could not read archived audits from {}", s3Uri, e);
            return null;
        }
    }

    private static byte[] decompressIfNeeded(byte[] data) throws IOException {
        boolean gzipped = data.length >= 2
                && data[0] == (byte) GZIPInputStream.GZIP_MAGIC
                && data[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8);
        if (!gzipped) {
            return data;
        }
        return new GZIPInputStream(new ByteArrayInputStream(data)).readAllBytes();
    }
}
