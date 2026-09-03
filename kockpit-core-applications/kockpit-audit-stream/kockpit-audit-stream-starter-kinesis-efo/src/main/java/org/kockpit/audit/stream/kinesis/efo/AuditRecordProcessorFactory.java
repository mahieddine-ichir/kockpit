package org.kockpit.audit.stream.kinesis.efo;

import lombok.RequiredArgsConstructor;
import org.kockpit.audit.stream.api.AuditConsumer;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;

import java.util.List;

@RequiredArgsConstructor
class AuditRecordProcessorFactory implements ShardRecordProcessorFactory {

    private final List<AuditConsumer> auditConsumers;

    private final int checkpointIntervalBatches;

    @Override
    public ShardRecordProcessor shardRecordProcessor() {
        return new AuditRecordProcessor(auditConsumers, checkpointIntervalBatches);
    }
}
