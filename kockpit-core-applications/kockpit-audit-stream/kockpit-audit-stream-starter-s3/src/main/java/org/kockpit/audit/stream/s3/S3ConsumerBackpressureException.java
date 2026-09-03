package org.kockpit.audit.stream.s3;

/**
 * Thrown by {@link S3AuditConsumer#accept} when its in-memory buffer is already at or over its
 * configured byte limit, instead of accepting (and buffering) more records. Deliberately
 * unchecked and left to propagate: every {@code AuditConsumer} caller (EFO/non-EFO Kinesis
 * record processors) only checkpoints/advances its cursor after {@code accept} returns
 * successfully, so letting this escape is what makes the caller redeliver the rejected batch
 * later - once S3/OpenSearch have drained the buffer - instead of losing it.
 */
public class S3ConsumerBackpressureException extends RuntimeException {

    public S3ConsumerBackpressureException(long bufferedBytes, long maxBufferedBytes) {
        super("S3 audit consumer buffer at %d bytes (limit %d) - rejecting new records until S3/OpenSearch drain it"
                .formatted(bufferedBytes, maxBufferedBytes));
    }
}
