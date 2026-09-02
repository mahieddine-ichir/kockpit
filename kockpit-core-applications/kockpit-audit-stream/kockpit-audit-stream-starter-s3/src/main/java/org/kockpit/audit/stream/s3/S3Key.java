package org.kockpit.audit.stream.s3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.kockpit.audit.stream.api.AuditStreamJson;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

@Data
public class S3Key {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
            .build();

    private String domain;
    private String env;
    private String appId;

    // Not part of the grouping key (see equals/hashCode below) - piggybacked here so ttl is
    // captured in the same decompress+parse pass as domain/env/appId, instead of a second one.
    private Integer ttl;

    // Also piggybacked: some producers (see S3OffloadingRecordCompressor upstream) already write
    // the full report - audits included - to their own S3 object before ever publishing to
    // Kinesis, and set this to that object's key. When present, this consumer must not archive
    // the (root-only, audits-stripped) wire record into its own batch and overwrite it - that
    // would replace the only pointer to the full record with one pointing at a strictly worse
    // copy. Named to not collide with this class's own identity as "the grouping key".
    @JsonProperty("s3Key")
    private String existingS3Key;

    // Wire records may be gzip-compressed (see AuditStreamJson); the S3 archive keeps the
    // original bytes as-is for byte-compatibility, but the key still needs to be parsed from
    // decompressed JSON.
    public static S3Key read(byte[] data) {
        try {
            return MAPPER.readValue(AuditStreamJson.read(data), S3Key.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String toString() {
        return "d=%s/e=%s/a=%s".formatted(domain, env, appId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        S3Key s3Key = (S3Key) o;
        return Objects.equals(domain, s3Key.domain) && Objects.equals(env, s3Key.env) && Objects.equals(appId, s3Key.appId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, env, appId);
    }
}
