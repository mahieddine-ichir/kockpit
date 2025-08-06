package com.accor.wcp.audit.module.kafka;

import static com.accor.wcp.audit.module.kafka.KafkaAuditInterceptorTest.TOPIC_1;
import static com.accor.wcp.audit.module.kafka.KafkaAuditInterceptorTest.TOPIC_AVRO;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.accor.wcp.audit.AuditEvent;
import com.accor.wcp.audit.AuditorEventService;
import com.accor.wcp.audit.AuditorService;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.kiss.formation.kafka.avro.Address;
import com.kiss.formation.kafka.avro.Company;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaString;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(
    controlledShutdown = true,
    topics = {TOPIC_1, TOPIC_AVRO},
    partitions = 1,
    adminTimeout = 60,
    kraft = false)
@ExtendWith(MockitoExtension.class)
@AutoConfigureWireMock(
    port = 0) // 0 is dynamic port which binds to the "wiremock.server.port" property
class KafkaAuditInterceptorTest {

  static final String TOPIC_1 = "topic1";

  static final String TOPIC_AVRO = "topic-avro";

  @Autowired private EmbeddedKafkaBroker embeddedKafkaBroker;

  @Value("${wiremock.server.port}")
  private String wiremockPort;

  @Captor ArgumentCaptor<List<AuditEvent>> auditEvents;

  @BeforeEach
  void before() throws Exception {
    ApplicationContext mockApplicationContext = Mockito.mock(ApplicationContext.class);
    KafkaAuditInterceptor.setApplicationContext(mockApplicationContext);
    AuditorEventService mockAuditorEventService = Mockito.mock(AuditorEventService.class);
    AuditorService mockAuditorService = Mockito.mock(AuditorService.class);
    when(mockApplicationContext.getBean(AuditorEventService.class))
        .thenReturn(mockAuditorEventService);
    when(mockApplicationContext.getBean(AuditorService.class)).thenReturn(mockAuditorService);
    doNothing().when(mockAuditorEventService).addAuditEvents(anyString(), auditEvents.capture());
    when(mockAuditorService.isAuditStarted()).thenReturn(true);

    setUpKafka();
  }

  public void setUpKafka() throws Exception {
    // Wait until the partitions are assigned.
    //    registry.getListenerContainers().stream().forEach(container ->
    //        ContainerTestUtils.waitForAssignment(container,
    // embeddedKafkaBroker.getPartitionsPerTopic()));
    //    testReceiver.counter.set(0);

    WireMock.reset();
    WireMock.resetAllRequests();
    WireMock.resetAllScenarios();
    WireMock.resetToDefault();

    registerSchema(1, TOPIC_AVRO, Company.getClassSchema().toString());
  }

  /**
   * Register the schema derived from the avro generated class for the given topic.
   *
   * @param schemaId the schema id to use
   * @param topic the topic name for the message schema to register
   * @param schema the schema JSON string
   */
  private void registerSchema(int schemaId, String topic, String schema) throws Exception {
    // Stub for the POST of the subject, to return the associated schemaId.
    // (The Avro schema, obtained by the serializer by reflection, will be in the body POSTed).
    // This is used by the Producer when serializing.
    // /subjects/send-payment-value?deleted=false
    stubFor(
        post(urlPathMatching("/subjects/" + topic + "-value/versions*"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"id\":" + schemaId + "}")));

    // Stub for the GET registered schema call for the given schema Id, returning the schema.
    // This is used by the Consumer when deserializing.
    // /schemas/ids/1?fetchMaxId=false
    final SchemaString schemaString = new SchemaString(schema);
    stubFor(
        get(urlPathMatching("/schemas/ids/" + schemaId))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(schemaString.toJson())));
  }

  @Test
  void should_audit_string() throws ExecutionException, InterruptedException {
    Properties producerProperties = new Properties();
    producerProperties.put(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    producerProperties.put(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    producerProperties.put(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
    producerProperties.put(
        ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, KafkaAuditInterceptor.class.getName());

    try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProperties)) {
      ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_1, "key1", "value1");
      record.headers().add("test1", "value test 1".getBytes());
      record.headers().add("test2", null);
      RecordMetadata recordMetadata = producer.send(record).get();
      recordMetadata = producer.send(record).get();

      List<List<AuditEvent>> auditEventsAllValues = auditEvents.getAllValues();
      assertThat(auditEventsAllValues).hasSize(4);
      assertThat(auditEventsAllValues.get(0).get(0))
          .hasFieldOrPropertyWithValue("source", KafkaMessageSource.PRODUCE)
          .hasFieldOrPropertyWithValue("key", record.key())
          .hasFieldOrPropertyWithValue("topic", record.topic())
          .hasFieldOrPropertyWithValue("payload", record.value())
          .hasFieldOrPropertyWithValue("keyClassname", String.class.getName())
          .hasFieldOrPropertyWithValue("payloadClassname", String.class.getName())
          .hasFieldOrProperty("timestamp")
          .hasFieldOrPropertyWithValue("headers", Map.of("test1", "value test 1"));
      assertThat(auditEventsAllValues.get(1).get(0))
          .hasFieldOrPropertyWithValue("source", KafkaMessageSource.PRODUCED_ACK)
          .hasFieldOrPropertyWithValue("partition", 0)
          .hasFieldOrPropertyWithValue("serializedKeySize", 4)
          .hasFieldOrPropertyWithValue("serializedValueSize", 6)
          .hasFieldOrPropertyWithValue("offset", 0L);

      assertThat(auditEventsAllValues.get(3).get(0))
          .hasFieldOrPropertyWithValue("source", KafkaMessageSource.PRODUCED_ACK)
          //          .hasFieldOrPropertyWithValue("partition", 0)
          //          .hasFieldOrPropertyWithValue("serializedKeySize", 4)
          //          .hasFieldOrPropertyWithValue("serializedValueSize", 6)
          .hasFieldOrPropertyWithValue("offset", 1L);
    }

    // Consume
    Properties consumerProperties = new Properties();
    consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, getClass().getName());
    consumerProperties.put(
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.EARLIEST.toString());
    consumerProperties.put(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    consumerProperties.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    consumerProperties.put(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
    consumerProperties.put(
        ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, KafkaAuditInterceptor.class.getName());
    try (KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(consumerProperties)) {
      kafkaConsumer.subscribe(List.of(TOPIC_1));
      ConsumerRecords<String, String> consumerRecords =
          kafkaConsumer.poll(Duration.of(10, ChronoUnit.SECONDS));

      assertThat(consumerRecords).hasSize(2);

      List<List<AuditEvent>> auditEventsAllValues = auditEvents.getAllValues();
      assertThat(auditEventsAllValues).hasSize(5);
      assertThat(auditEventsAllValues.get(4).get(0))
          .hasFieldOrPropertyWithValue("source", KafkaMessageSource.CONSUME)
          .hasFieldOrPropertyWithValue("partition", 0)
          .hasFieldOrPropertyWithValue("serializedKeySize", 4)
          .hasFieldOrPropertyWithValue("serializedValueSize", 6)
          .hasFieldOrPropertyWithValue("offset", 0L);
      assertThat(auditEventsAllValues.get(4).get(1))
          .hasFieldOrPropertyWithValue("source", KafkaMessageSource.CONSUME)
          .hasFieldOrPropertyWithValue("partition", 0)
          .hasFieldOrPropertyWithValue("serializedKeySize", 4)
          .hasFieldOrPropertyWithValue("serializedValueSize", 6)
          .hasFieldOrPropertyWithValue("offset", 1L);
    }
  }

  @Test
  void should_audit_avro() throws ExecutionException, InterruptedException {
    String schemaRegistryUrl = "http://localhost:" + wiremockPort + "/";
    Properties producerProperties = new Properties();
    producerProperties.put(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    producerProperties.put(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
    producerProperties.put(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
    producerProperties.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
    producerProperties.put(
        ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, KafkaAuditInterceptor.class.getName());

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

    try (KafkaProducer<String, Company> producer = new KafkaProducer<>(producerProperties)) {
      ProducerRecord<String, Company> record =
          new ProducerRecord<>(TOPIC_AVRO, "key1", companyFake);
      RecordMetadata recordMetadata = producer.send(record).get();

      List<List<AuditEvent>> auditEventsAllValues = auditEvents.getAllValues();
      assertThat(auditEventsAllValues).hasSize(2);
      assertThat(auditEventsAllValues.get(0).get(0))
          .hasFieldOrPropertyWithValue("source", KafkaMessageSource.PRODUCE)
          .hasFieldOrPropertyWithValue("key", record.key())
          .hasFieldOrPropertyWithValue("topic", record.topic())
          .hasFieldOrPropertyWithValue("payload", record.value().toString())
          .hasFieldOrPropertyWithValue("keyClassname", String.class.getName())
          .hasFieldOrPropertyWithValue("payloadClassname", Company.class.getName())
          .hasFieldOrProperty("timestamp");
      assertThat(auditEventsAllValues.get(1).get(0))
          .hasFieldOrPropertyWithValue("source", KafkaMessageSource.PRODUCED_ACK)
          .hasFieldOrPropertyWithValue("partition", 0)
          .hasFieldOrPropertyWithValue("serializedKeySize", 4)
          .hasFieldOrPropertyWithValue("serializedValueSize", 47)
          .hasFieldOrPropertyWithValue("offset", 0L);
    }

    // Consume
    Properties consumerProperties = new Properties();
    consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, getClass().getName());
    consumerProperties.put(
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.EARLIEST.toString());
    consumerProperties.put(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    consumerProperties.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
    consumerProperties.put(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
    consumerProperties.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
    consumerProperties.put(
        ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, KafkaAuditInterceptor.class.getName());

    try (KafkaConsumer<String, Company> kafkaConsumer = new KafkaConsumer<>(consumerProperties)) {
      kafkaConsumer.subscribe(List.of(TOPIC_AVRO));
      ConsumerRecords<String, Company> consumerRecords =
          kafkaConsumer.poll(Duration.of(10, ChronoUnit.SECONDS));

      assertThat(consumerRecords).hasSize(1);

      List<List<AuditEvent>> auditEventsAllValues = auditEvents.getAllValues();
      assertThat(auditEventsAllValues).hasSize(3);
      assertThat(auditEventsAllValues.get(2).get(0))
          .hasFieldOrPropertyWithValue("source", KafkaMessageSource.CONSUME)
          .hasFieldOrPropertyWithValue("partition", 0)
          .hasFieldOrPropertyWithValue("payload", companyFake.toString())
          .hasFieldOrPropertyWithValue("serializedKeySize", 4)
          .hasFieldOrPropertyWithValue("serializedValueSize", 47)
          .hasFieldOrPropertyWithValue("offset", 0L);
    }
  }
}
