package org.kockpit.audit.stream.s3;

import lombok.Getter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class S3Batch {

    @Getter
    private final Queue<S3Record> records = new ConcurrentLinkedQueue<>();

    // ConcurrentLinkedQueue.size() is O(n) - tracked separately so accept() can cheaply check
    // "did this batch just reach batchSize" on every add without walking the whole queue.
    private final AtomicInteger size = new AtomicInteger();

    // offset/length are assigned later, per S3 object, in S3AuditConsumer.write() - a record's
    // position within the eventually-written object isn't known yet here (records for the same
    // key accumulate across many flush cycles, each writing its own separate object).
    // Returns the batch's size right after this add, so callers can react to crossing a threshold
    // without a second, separately-racing size() read.
    int add(S3Record record) {
        records.add(record);
        return size.incrementAndGet();
    }

    public boolean isEmpty() {
        return records.isEmpty();
    }

    public S3Record poll() {
        S3Record record = records.poll();
        if (record != null) {
            size.decrementAndGet();
        }
        return record;
    }
}
