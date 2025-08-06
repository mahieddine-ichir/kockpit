package com.accor.wcp.sample.kafka;

import com.accor.wcp.audit.module.kafka.KafkaAuditInterceptor;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.kiss.formation.kafka.avro.Company;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import java.util.Properties;

@Configuration
class KafkaProducerConfig {

    @Autowired
    private WireMockServer schemaRegistryMock;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Bean
    public KafkaProducer<String, String> kafkaProducer() {
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        producerProperties.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, KafkaAuditInterceptor.class.getName());
        return new KafkaProducer<>(producerProperties);
    }

    @Bean
    public KafkaProducer<String, Company> kafkaAvroProducer() {
        String schemaRegistryUrl = schemaRegistryMock.baseUrl() + "/";
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        producerProperties.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, KafkaAuditInterceptor.class.getName());
        producerProperties.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        return new KafkaProducer<>(producerProperties);
    }
}
