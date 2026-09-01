package org.kockpit.audit.stream.s3;

import lombok.Getter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class S3Batch {

    @Getter
    private final Queue<S3Record> records = new ConcurrentLinkedQueue<>();

    // offset/length are assigned later, per S3 object, in S3AuditConsumer.write() - a record's
    // position within the eventually-written object isn't known yet here (records for the same
    // key accumulate across many flush cycles, each writing its own separate object).
    void add(S3Record record) {
        records.add(record);
    }

    public boolean isEmpty() {
        return records.isEmpty();
    }

    public S3Record poll() {
        return records.poll();
    }
}
