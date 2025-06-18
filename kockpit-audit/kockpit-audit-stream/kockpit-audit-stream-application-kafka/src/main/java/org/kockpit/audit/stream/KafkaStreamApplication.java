package org.kockpit.audit.stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class KafkaStreamApplication implements CommandLineRunner {

    private final KafkaProperties kafkaProperties;

    public static void main(String[] args) {
        SpringApplication.run(KafkaStreamApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("""
            Consumer properties:
                Bootstrap Servers: {},
                SASL Protocol: {},
                Consumer group: {}
        """,
                kafkaProperties.getBootstrapServers(),
                kafkaProperties.getSecurity().getProtocol(),
                kafkaProperties.getConsumer().getGroupId()
        );
    }
}