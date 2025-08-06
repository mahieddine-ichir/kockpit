package com.accor.wcp.console.services.core.application.heartbit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;

@Component
@RequiredArgsConstructor
class HeartBitRecordProcessorFactory implements ShardRecordProcessorFactory {

  private final HeartBitInMemoryManager heartBitInMemoryManager;

  @Override
  public ShardRecordProcessor shardRecordProcessor() {
    return new HeartBitRecordProcessor(heartBitInMemoryManager);
  }
}
