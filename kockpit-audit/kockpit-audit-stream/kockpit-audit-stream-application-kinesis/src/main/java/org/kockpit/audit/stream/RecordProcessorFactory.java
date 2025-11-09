package org.kockpit.audit.stream;

import lombok.RequiredArgsConstructor;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.springframework.stereotype.Component;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;

@Component
@RequiredArgsConstructor
class RecordProcessorFactory implements ShardRecordProcessorFactory {

    private final AuditRequestConverter auditRequestConverter;
    private final AuditConsumer auditConsumer;

    @Override
    public ShardRecordProcessor shardRecordProcessor() {
        return new RecordProcessor(auditRequestConverter, auditConsumer);
    }
}
