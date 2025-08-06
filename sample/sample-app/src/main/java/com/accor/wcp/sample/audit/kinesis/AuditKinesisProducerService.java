package com.accor.wcp.sample.audit.kinesis;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;

@Component
public class AuditKinesisProducerService {

  private final KinesisClient kinesisClient;

  @Value("${kinesis.streamName:}")
  private String streamName;

  public AuditKinesisProducerService(@Qualifier("kinesisClient") KinesisClient kinesisClient) {
    this.kinesisClient = kinesisClient;
  }

  public List<String> sendToKinesis(int times) {
    List<String> results = new ArrayList<>();

    for (int i = 0; i < times; i++) {
      KinesisMessage kinesisMessage =
          new KinesisMessage(
              "partitionkey-" + i,
              "{'kinesisMessage':'kinesisMessage', 'kinesisMessage2':'kinesisMessage'}");
      results.add("Send kinesis message " + i + " times");
      produce(streamName, kinesisMessage);
    }
    return results;
  }

  public String sendBigReportToKinesis() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();

    InputStream in =
        Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("wcp_samples_big_report.json");
    Object bigReport = objectMapper.readValue(in, Object.class);
    KinesisMessage kinesisMessage =
        new KinesisMessage(
            "partitionkey",
            bigReport.toString());
    produce(streamName, kinesisMessage);
    return "Big report message send to kinesis";
  }

  public void produce(String streamName, KinesisMessage kinesisMessage) {
    PutRecordRequest putRecordRequest =
        PutRecordRequest.builder()
            .partitionKey(kinesisMessage.getPartitionkey())
            .streamName(streamName)
            .data(SdkBytes.fromByteArray(kinesisMessage.getPayload().getBytes()))
            .build();

    kinesisClient.putRecord(putRecordRequest);
  }
}
