package org.kockpit.audit.notification.kafka;

import org.springframework.boot.autoconfigure.AutoConfiguration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AutoConfiguration
@ConditionalOnProperty(
        value = "kockpit.audit.kafka.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Slf4j
public class KafkaAuditServiceAutoConfiguration {

  @Bean
  AuditReportNotificationService kafkaAuditReportNotificationService(
          KafkaTemplate<String, byte[]> kafkaTemplate,
          @Value("${kockpit.audit.notification.kafka.topic}") String topic) {
    log.info("✅ Kafka Audit notification topic: {}", topic);
    return new KafkaAuditReportNotificationService(kafkaTemplate, topic);
  }

  @Bean
  ProducerFactory<String, byte[]> producerFactory(KafkaProperties kafkaProperties) {
    List<String> producerBootstrapServers = kafkaProperties.getProducer().getBootstrapServers();
    List<String> bootstrapServers = (producerBootstrapServers != null && !producerBootstrapServers.isEmpty())
            ? producerBootstrapServers
            : kafkaProperties.getBootstrapServers();
    log.info("✅ Kafka Audit producer bootstrap servers: {}", bootstrapServers);
    Map<String, Object> configProps = new HashMap<>(kafkaProperties.getProducer().getProperties());
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean
  KafkaTemplate<String, byte[]> kafkaTemplate(ProducerFactory<String, byte[]> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }
}
