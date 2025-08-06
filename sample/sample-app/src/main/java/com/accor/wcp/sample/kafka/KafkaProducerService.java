package com.accor.wcp.sample.kafka;

import com.kiss.formation.kafka.avro.Address;
import com.kiss.formation.kafka.avro.Company;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.accor.wcp.sample.kafka.Topics.WCPSAMPLES_TOPIC_AVRO;
import static com.accor.wcp.sample.kafka.Topics.WCPSAMPLES_TOPIC_JSON;
import static com.accor.wcp.sample.kafka.Topics.WCPSAMPLES_TOPIC_XML;

@Service
class KafkaProducerService {

    @Autowired
    private KafkaProducer<String, String> kafkaProducer;

    @Autowired
    private KafkaProducer<String, Company> kafkaAvroProducer;

    public String produceAllMessages() throws IOException, URISyntaxException {
        return "XML=" + produceMessageXML() + "<br><br>" + "JSON=" + produceMessageJSON() + "<br><br>" + "AVRO=" + produceMessageAvro();
    }

    public String produceMessageXML() throws IOException, URISyntaxException {
        String xmlData = Files.readString(Path.of(getClass().getResource("/data/basket1.xml").toURI()));
        ProducerRecord<String, String> record = new ProducerRecord<>(WCPSAMPLES_TOPIC_XML.getName(), "key", xmlData);
        kafkaProducer.send(record);
        return record.toString();
    }

    public String produceMessageJSON() throws IOException, URISyntaxException {
        String jsonData = Files.readString(Path.of(getClass().getResource("/data/kafka_order_spi_hs_tars_bookitems_request.json").toURI()));
        ProducerRecord<String, String> record = new ProducerRecord<>(WCPSAMPLES_TOPIC_JSON.getName(), "key", jsonData);
        kafkaProducer.send(record);
        return record.toString();
    }

    public String produceMessageAvro() {
        Company companyFake =
            Company.newBuilder()
                .setName("FakeOne")
                .setFirstName("Bob")
                .setLastName("Jones")
                .setYearOfCreation(2017)
                .setAddress(
                    Address.newBuilder()
                        .setAddress1("Address1")
                        .setCity("Paris")
                        .setZipCode(75000)
                        .build())
                .build();
        ProducerRecord<String, Company> record = new ProducerRecord<>(WCPSAMPLES_TOPIC_AVRO.getName(), "key", companyFake);
        kafkaAvroProducer.send(record);

        return record.toString();
    }
}
