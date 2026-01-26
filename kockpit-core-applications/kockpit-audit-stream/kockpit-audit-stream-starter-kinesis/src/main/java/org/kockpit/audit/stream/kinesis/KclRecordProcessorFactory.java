package org.kockpit.audit.stream.kinesis;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;

@RequiredArgsConstructor
public class KclRecordProcessorFactory implements ShardRecordProcessorFactory {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public ShardRecordProcessor shardRecordProcessor() {
        return new KclRecordProcessor(applicationEventPublisher);
    }
}
