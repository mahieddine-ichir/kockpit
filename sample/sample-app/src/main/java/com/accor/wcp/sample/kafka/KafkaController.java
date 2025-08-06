package com.accor.wcp.sample.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;

@RestController
class KafkaController {

    @Autowired
    private KafkaProducerService kafkaProducerService;
    @Autowired
    private KafkaConsumerService kafkaConsumerService;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @GetMapping("/kafka/topics")
    public ResponseEntity<String> checkKafkaTopics() {
        return ResponseEntity.ok(embeddedKafkaBroker.getTopics().toString());
    }

    @GetMapping("/kafka/produce/all")
    public ResponseEntity<String> produceAllMessages() throws IOException, URISyntaxException {
        return ResponseEntity.ok(kafkaProducerService.produceAllMessages());
    }

    @GetMapping("/kafka/produce/xml")
    public ResponseEntity<String> produceMessageXml() throws IOException, URISyntaxException {
        return ResponseEntity.ok(kafkaProducerService.produceMessageXML());
    }

    @GetMapping("/kafka/produce/json")
    public ResponseEntity<String> produceMessageJson() throws IOException, URISyntaxException {
        return ResponseEntity.ok(kafkaProducerService.produceMessageJSON());
    }

    @GetMapping("/kafka/produce/avro")
    public ResponseEntity<String> produceMessageAvro() {
        return ResponseEntity.ok(kafkaProducerService.produceMessageAvro());
    }

    @GetMapping("/kafka/consume/xml")
    public ResponseEntity<String> consumeMessageXml() {
        return ResponseEntity.ok(kafkaConsumerService.consumeXML());
    }

    @GetMapping("/kafka/consume/json")
    public ResponseEntity<String> consumeMessageJson() {
        return ResponseEntity.ok(kafkaConsumerService.consumeJSON());
    }

    @GetMapping("/kafka/consume/all")
    public ResponseEntity<String> consumeallMessages() {
        return ResponseEntity.ok(kafkaConsumerService.consumeAll());
    }

    @GetMapping("/kafka/consume/avro")
    public ResponseEntity<String> consumeMessageAvro() {
        return ResponseEntity.ok(kafkaConsumerService.consumeAvro());
    }
}
