package org.kockpit.audit.stream.s3;

/**
 * Thrown by {@link S3AuditConsumer#accept} only if the calling thread is interrupted while parked
 * in {@code awaitBufferRoom()} waiting for its in-memory buffer to drain back under its configured
 * byte limit - never for the wait itself, which blocks (not rejects) callers until there's room.
 * Blocking, not rejecting-then-moving-on, is what actually backpressures a push-based Kinesis EFO
 * subscription: not returning from {@code accept} is what holds up the caller's next
 * {@code subscription.request(1)} and checkpoint, since the subscription keeps advancing to new
 * records (and KCL's checkpoint ceiling keeps advancing with it) regardless of whether a thrown
 * exception is caught somewhere - an exception on every call would just let it race ahead and
 * silently skip whatever was rejected in between.
 */
public class S3ConsumerBackpressureException extends RuntimeException {

    public S3ConsumerBackpressureException(long bufferedBytes, long maxBufferedBytes, InterruptedException cause) {
        super("Interrupted while waiting for S3 audit consumer buffer to drain (%d bytes buffered, limit %d)"
                .formatted(bufferedBytes, maxBufferedBytes), cause);
    }
}
