package com.accor.wcp.console.services.core.application.heartbit;

import com.accor.wcp.aws.kinesis.consumer.AbstractConsumerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty("wcp.sdk.communication.heartbit.stream_name")
class HeartBitConsumerConfig extends AbstractConsumerConfig {

  public HeartBitConsumerConfig(@Value(value = "${wcp.sdk.communication.heartbit.stream_name}") String streamName,
                                @Value(value = "${application.id}") String applicationId,
                                HeartBitRecordProcessorFactory recordProcessorFactory) {
    super(streamName, applicationId + "-heartbit", recordProcessorFactory);
  }

}
