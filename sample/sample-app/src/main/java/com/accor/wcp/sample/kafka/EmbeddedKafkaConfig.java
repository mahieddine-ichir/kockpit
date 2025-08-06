package com.accor.wcp.sample.kafka;

import static com.accor.wcp.sample.kafka.Topics.WCPSAMPLES_TOPIC_AVRO;
import static com.accor.wcp.sample.kafka.Topics.WCPSAMPLES_TOPIC_JSON;
import static com.accor.wcp.sample.kafka.Topics.WCPSAMPLES_TOPIC_XML;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.kiss.formation.kafka.avro.Company;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaString;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;

@Configuration
class EmbeddedKafkaConfig {

    @Bean
    public EmbeddedKafkaBroker embeddedKafkaBroker() {
    return new EmbeddedKafkaZKBroker(
        1,
        true,
        1,
        WCPSAMPLES_TOPIC_XML.getName(),
        WCPSAMPLES_TOPIC_JSON.getName(),
        WCPSAMPLES_TOPIC_AVRO.getName());
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer schemaRegistryMock() throws IOException {
        WireMockServer wireMockServer = new WireMockServer(0);
        wireMockServer.stubFor(
            post(urlPathMatching("/subjects/" + WCPSAMPLES_TOPIC_AVRO.getName() + "-value/versions*"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":" + 1 + "}")));

        SchemaString schemaString = new SchemaString(Company.getClassSchema().toString());
        wireMockServer.stubFor(
            get(urlPathMatching("/schemas/ids/" + 1))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(schemaString.toJson())));
        return wireMockServer;
    }
}
