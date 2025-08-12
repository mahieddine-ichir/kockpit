package com.accor.wcp.services.auditstream.notification.kinesis;

import com.accor.wcp.aws.kinesis.consumer.AbstractConsumerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty("aws.stream_name")
class ConsumerConfig extends AbstractConsumerConfig {

  protected ConsumerConfig(@Value(value = "${aws.stream_name}") String streamName,
                           @Value(value = "${application.name}") String applicationId,
                           RecordProcessorFactory recordProcessorFactory) {
    super(streamName, applicationId, recordProcessorFactory);
  }
}
