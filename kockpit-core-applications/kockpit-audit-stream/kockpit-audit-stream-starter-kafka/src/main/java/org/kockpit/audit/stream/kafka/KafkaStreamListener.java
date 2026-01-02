package org.kockpit.audit.stream.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.kockpit.audit.stream.api.AuditConsumerEvent;
import org.kockpit.audit.stream.api.model.AuditReport;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

@Configuration
@EnableKafka
@RequiredArgsConstructor
@Slf4j
public class KafkaStreamListener {

    private final ObjectMapper objectMapper;

    private final ApplicationEventPublisher applicationEventPublisher;

    @KafkaListener(topics = "${kockpit.audit.stream.kafka.topics}")
    void processAudit(byte[] message) {
            String event = null;
            try {
                event = read(message);
                AuditReport audit = objectMapper.readValue(event, AuditReport.class);
                applicationEventPublisher.publishEvent(new AuditConsumerEvent(audit));
            } catch (Exception e) {
                log.error("Error processing audit message. Event: {}. Error: {}", event, e.getMessage(), e);
            }
    }

    String read(byte[] message) throws IOException {
        // Check if the data is GZIP compressed by checking the magic number
        // GZIP files start with 0x1f 0x8b
        if (message.length >= 2 && message[0] == (byte) 0x1f && message[1] == (byte) 0x8b) {
            log.debug("Detected GZIP compressed data, decompressing...");
            return decompress(message);
        } else {
            log.debug("Data is not compressed, converting directly to string");
            return new String(message, StandardCharsets.UTF_8);
        }
    }

    private String decompress(byte[] compressedData) throws IOException {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipStream = new GZIPInputStream(byteStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }

            byte[] decompressed = outputStream.toByteArray();
            log.debug("Decompressed {} bytes to {} bytes", compressedData.length, decompressed.length);

            return new String(decompressed, StandardCharsets.UTF_8);
        }
    }

    @Bean
    public ConsumerFactory<String, byte[]> consumerFactory(KafkaProperties properties) {
        Map<String, Object> props = new HashMap<>(properties.buildConsumerProperties());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, byte[]> kafkaListenerContainerFactory(KafkaProperties kafkaProperties) {
        ConcurrentKafkaListenerContainerFactory<String, byte[]> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(kafkaProperties));
        return factory;
    }
}
