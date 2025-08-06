package com.accor.wcp.sample.kafka;

import com.kiss.formation.kafka.avro.Company;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.accor.wcp.sample.kafka.Topics.WCPSAMPLES_TOPIC_AVRO;
import static com.accor.wcp.sample.kafka.Topics.WCPSAMPLES_TOPIC_JSON;
import static com.accor.wcp.sample.kafka.Topics.WCPSAMPLES_TOPIC_XML;


@Service
class KafkaConsumerService {

    @Autowired
    private KafkaConsumer<String, String> kafkaConsumer;

    @Autowired
    private KafkaConsumer<String, Company> kafkaAvroConsumer;

    public String consumeAll() {
        kafkaConsumer.subscribe(List.of(WCPSAMPLES_TOPIC_XML.getName(), WCPSAMPLES_TOPIC_JSON.getName()));
        ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.of(10, ChronoUnit.SECONDS));
        kafkaAvroConsumer.subscribe(List.of(WCPSAMPLES_TOPIC_AVRO.getName()));
        ConsumerRecords<String, Company> consumerRecordsAvro = kafkaAvroConsumer.poll(Duration.of(10, ChronoUnit.SECONDS));
        Integer count = consumerRecords.count() + consumerRecordsAvro.count();
        return "Consumed " + count + " message(s) from all topics";
    }

    public String consumeXML() {
        kafkaConsumer.subscribe(List.of(WCPSAMPLES_TOPIC_XML.getName()));
        ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.of(10, ChronoUnit.SECONDS));
        return "Consumed " + consumerRecords.count() + " message(s) from " + WCPSAMPLES_TOPIC_XML.getName();
    }

    public String consumeJSON() {
        kafkaConsumer.subscribe(List.of(WCPSAMPLES_TOPIC_JSON.getName()));
        ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.of(10, ChronoUnit.SECONDS));
        return "Consumed " + consumerRecords.count() + " message(s) from " + WCPSAMPLES_TOPIC_JSON.getName();
    }

    public String consumeAvro() {
        kafkaAvroConsumer.subscribe(List.of(WCPSAMPLES_TOPIC_AVRO.getName()));
        ConsumerRecords<String, Company> consumerRecords = kafkaAvroConsumer.poll(Duration.of(10, ChronoUnit.SECONDS));
        return "Consumed " + consumerRecords.count() + " message(s) from " + WCPSAMPLES_TOPIC_AVRO.getName();
    }
}
