package com.accor.wcp.sample.kafka;

import com.accor.wcp.audit.module.kafka.KafkaAuditInterceptor;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.kiss.formation.kafka.avro.Company;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import java.util.Properties;

@EnableKafka
@Configuration
class KafkaConsumerConfig {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private WireMockServer schemaRegistryMock;

    @Bean
    public KafkaConsumer<String, String> kafkaConsumer() {
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "STRING");
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.EARLIEST.toString());
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        consumerProperties.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, KafkaAuditInterceptor.class.getName());
        return new KafkaConsumer<>(consumerProperties);
    }

    @Bean
    public KafkaConsumer<String, Company> kafkaAvroConsumer() {
        String schemaRegistryUrl = schemaRegistryMock.baseUrl() + "/";
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "AVRO");
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.EARLIEST.toString());
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        consumerProperties.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, KafkaAuditInterceptor.class.getName());
        consumerProperties.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        return new KafkaConsumer<>(consumerProperties);
    }
}
