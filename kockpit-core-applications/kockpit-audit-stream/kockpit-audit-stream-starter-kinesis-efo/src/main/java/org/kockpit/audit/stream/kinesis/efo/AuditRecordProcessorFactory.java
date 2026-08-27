package org.kockpit.audit.stream.kinesis.efo;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;

@RequiredArgsConstructor
class AuditRecordProcessorFactory implements ShardRecordProcessorFactory {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public ShardRecordProcessor shardRecordProcessor() {
        return new AuditRecordProcessor(new EfoRecordProcessor(), applicationEventPublisher);
    }
}
