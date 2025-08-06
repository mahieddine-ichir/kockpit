package com.accor.wcp.audit.module.kafka;

import static com.accor.wcp.audit.module.kafka.KafkaAudit.TYPE;
import static com.accor.wcp.audit.module.kafka.KafkaMessageSource.CONSUME;
import static com.accor.wcp.audit.module.kafka.KafkaMessageSource.PRODUCE;
import static com.accor.wcp.audit.module.kafka.KafkaMessageSource.PRODUCED_ACK;
import static java.util.Objects.nonNull;

import com.accor.wcp.audit.AuditorEventService;
import com.accor.wcp.audit.AuditorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.context.ApplicationContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * Kafka Client producer/consumer interceptor to audit produced/consumed messages. To use it,
 * reference it in your producer/consumer config through:
 * interceptor.classes=com.accor.wcp.audit.module.kafka.KafkaProducerAuditInterceptor
 */
@Slf4j
public class KafkaAuditInterceptor
    implements ProducerInterceptor<Object, Object>, ConsumerInterceptor<Object, Object> {

  static ApplicationContext context;

  public static void setApplicationContext(ApplicationContext context) {
    KafkaAuditInterceptor.context = context;
  }

  @Override
  public ProducerRecord<Object, Object> onSend(ProducerRecord<Object, Object> producerRecord) {
    KafkaAudit kafkaAudit = auditProducerRecord(producerRecord);
    AuditorEventService auditorEventService = context.getBean(AuditorEventService.class);
    auditorEventService.addAuditEvents(TYPE, List.of(kafkaAudit));
    return producerRecord;
  }

  private KafkaAudit auditProducerRecord(ProducerRecord<Object, Object> producerRecord) {
    Object value = producerRecord.value();
    Object key = producerRecord.key();
    String payload = null;
    String payloadClassname = null;
    if (nonNull(value)) {
      // TODO - must we convert to json?
      payload = value.toString();
      payloadClassname = value.getClass().getName();
    }
    String keyValue = null;
    String keyClassname = null;
    if (nonNull(key)) {
      keyValue = key.toString();
      keyClassname = key.getClass().getName();
    }
    return KafkaAudit.builder()
        .source(PRODUCE)
        .key(keyValue)
        .keyClassname(keyClassname)
        .payloadClassname(payloadClassname)
        .payload(payload)
        .topic(producerRecord.topic())
        .partition(producerRecord.partition())
        .timestamp(producerRecord.timestamp())
        .headers(getHeaders(producerRecord.headers()))
        .build();
  }

  @Override
  public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
    // onAcknowledgement is executed in future thread
    // We should find a way to link onSend and onAcknowledgement
    AuditorService auditorService = context.getBean(AuditorService.class);
    if (!auditorService.isAuditStarted()) {
      return;
    }

    KafkaAudit kafkaAudit =
        KafkaAudit.builder()
            .source(PRODUCED_ACK)
            .topic(metadata.topic())
            .partition(metadata.partition())
            .timestamp(metadata.timestamp())
            .serializedKeySize(metadata.serializedKeySize())
            .serializedValueSize(metadata.serializedValueSize())
            .offset(metadata.offset())
            .build();
    AuditorEventService auditorEventService = context.getBean(AuditorEventService.class);
    auditorEventService.addAuditEvents(TYPE, List.of(kafkaAudit));
  }

  @Override
  public ConsumerRecords<Object, Object> onConsume(ConsumerRecords<Object, Object> records) {
    List<KafkaAudit> kafkaAudits =
        StreamSupport.stream(records.spliterator(), true).map(this::auditConsumedRecord).toList();

    AuditorEventService auditorEventService = context.getBean(AuditorEventService.class);
    auditorEventService.addAuditEvents(TYPE, new ArrayList<>(kafkaAudits));
    return records;
  }

  private KafkaAudit auditConsumedRecord(ConsumerRecord<Object, Object> consumerRecord) {
    Object value = consumerRecord.value();
    Object key = consumerRecord.key();
    String payload = null;
    String payloadClassname = null;
    if (nonNull(value)) {
      // TODO - must we convert to json?
      payload = value.toString();
      payloadClassname = value.getClass().getName();
    }
    String keyValue = null;
    String keyClassname = null;
    if (nonNull(key)) {
      keyValue = key.toString();
      keyClassname = key.getClass().getName();
    }
    return KafkaAudit.builder()
        .source(CONSUME)
        .key(keyValue)
        .keyClassname(keyClassname)
        .payloadClassname(payloadClassname)
        .payload(payload)
        .topic(consumerRecord.topic())
        .partition(consumerRecord.partition())
        .offset(consumerRecord.offset())
        .timestamp(consumerRecord.timestamp())
        .serializedKeySize(consumerRecord.serializedKeySize())
        .serializedValueSize(consumerRecord.serializedValueSize())
        .headers(getHeaders(consumerRecord.headers()))
        .build();
  }

  private Map<String, String> getHeaders(Headers headers) {

    Map<String, String> mapHeaders = new HashMap<>();
    for(Header h : headers) {
      if (nonNull(h.value())) {
        mapHeaders.put(h.key(), new String(h.value()));
      }
    }

    return mapHeaders.isEmpty() ? null : mapHeaders;
  }

  @Override
  public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets) {
    // Nothing to audit here
  }

  @Override
  public void close() {
    // Nothing to close
  }

  @Override
  public void configure(Map<String, ?> configs) {
    // Nothing to configure
  }
}
