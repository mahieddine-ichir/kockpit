package com.accor.wcp.console.services.core.communication.app2wcp;

import com.accor.wcp.aws.kinesis.consumer.AbstractConsumerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty("wcp.sdk.communication.app2wcp.stream_name")
class App2WcpConsumerConfig extends AbstractConsumerConfig {

  public App2WcpConsumerConfig(@Value(value = "${wcp.sdk.communication.app2wcp.stream_name}") String streamName,
                                @Value(value = "${application.id}") String applicationId,
                                App2WcpRecordProcessorFactory recordProcessorFactory) {
    super(streamName, applicationId + "-app2wcp", recordProcessorFactory);
  }
}
