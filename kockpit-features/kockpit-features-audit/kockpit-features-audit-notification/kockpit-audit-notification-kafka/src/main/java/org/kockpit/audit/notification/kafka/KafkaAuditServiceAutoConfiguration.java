package org.kockpit.audit.notification.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.kockpit.audit.api.CompressionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaAuditServiceAutoConfiguration {

  @Bean
  CompressionService compressionService(
          @Value("${kockpit.audit.compression.enabled:true}") boolean compressionEnabled) {
    return new CompressionService(compressionEnabled);
  }

  @Bean
  AuditReportNotificationService kafkaAuditReportNotificationService(
          KafkaTemplate<String, byte[]> kafkaTemplate,
          @Value("${kockpit.audit.notification.kafka.topic}") String topic,
          CompressionService compressionService) {
    return new KafkaAuditReportNotificationService(kafkaTemplate, topic, compressionService);
  }

  @Bean
  ProducerFactory<String, byte[]> producerFactory(KafkaProperties kafkaProperties) {
    Map<String, Object> configProps = new HashMap<>(kafkaProperties.getProducer().getProperties());
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean
  KafkaTemplate<String, byte[]> kafkaTemplate(KafkaProperties kafkaProperties) {
    return new KafkaTemplate<>(producerFactory(kafkaProperties));
  }
}
